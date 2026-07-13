package com.aianik.anik.ai.features.rag.pipeline;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.common.execption.AnikAiException;
import com.aianik.anik.ai.features.resource.ResourceService;
import com.aianik.anik.ai.persistence.rag.mapper.RagMapper;
import com.aianik.anik.ai.persistence.rag.mapper.RagChunkMapper;
import com.aianik.anik.ai.persistence.rag.mapper.RagDocumentMapper;
import com.aianik.anik.ai.features.rag.dto.ChunkDTO;
import com.aianik.anik.ai.features.rag.enums.RagDocumentStatus;
import com.aianik.anik.ai.features.rag.util.ContentHashUtil;
import com.aianik.anik.ai.persistence.rag.po.RagPO;
import com.aianik.anik.ai.persistence.rag.po.RagChunkPO;
import com.aianik.anik.ai.persistence.rag.po.RagDocumentPO;
import com.aianik.anik.ai.search.storage.search.SearchEngineFactory;
import com.aianik.anik.ai.search.storage.search.api.SearchAddRequest;
import com.aianik.anik.ai.search.storage.search.api.SearchDocument;
import com.aianik.anik.ai.search.storage.search.api.SearchEngine;
import com.aianik.anik.ai.search.storage.search.constant.SearchMetadataKeys;
import com.aianik.anik.ai.features.rag.strategy.parser.DocumentParser;
import com.aianik.anik.ai.features.rag.strategy.parser.DocumentParserFactory;
import com.aianik.anik.ai.vector.storage.vector.VectorStoreFactory;
import com.aianik.anik.ai.vector.storage.vector.api.IndexNameBuilder;
import com.aianik.anik.ai.vector.storage.vector.api.VectorAddRequest;
import com.aianik.anik.ai.vector.storage.vector.api.VectorDocument;
import com.aianik.anik.ai.vector.storage.vector.api.AnikAiVectorStore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentPipeline {

    private final RagDocumentMapper ragDocumentMapper;
    private final RagChunkMapper ragChunkMapper;
    private final RagMapper knowledgeMapper;
    private final DocumentParserFactory parserFactory;
    private final DocumentChunkingService documentChunkingService;
    private final VectorStoreFactory vectorStoreFactory;
    private final SearchEngineFactory searchEngineFactory;
    private final TransactionTemplate transactionTemplate;
    private final ResourceService resourceService;

    public void processDocument(Long documentId) {
        RagDocumentPO document = ragDocumentMapper.selectById(documentId);
        if (document == null) {
            log.error("Document not found: {}", documentId);
            return;
        }

        RagPO knowledge = knowledgeMapper.selectById(document.getRagId());
        if (knowledge == null) {
            log.error("Knowledge not found: {} for document: {}", document.getRagId(), documentId);
            updateStatus(documentId, RagDocumentStatus.FAILED, "Knowledge not found");
            return;
        }

        try {
            updateStatus(documentId, RagDocumentStatus.PROCESSING, null);
            log.info("Start processing document: [{}] {}", documentId, document.getName());

            String content = parseContent(document);
            if (StrUtil.isBlank(content)) {
                updateStatus(documentId, RagDocumentStatus.FAILED, "Empty content after parsing");
                return;
            }

            cleanExistingData(knowledge, documentId);

            List<ChunkDTO> chunks = documentChunkingService.chunk(content, knowledge);

            if (chunks.isEmpty()) {
                updateStatus(documentId, RagDocumentStatus.FAILED, "No chunks generated");
                return;
            }

            List<RagChunkPO> chunkPOs = new ArrayList<>();
            for (ChunkDTO chunk : chunks) {
                chunkPOs.add(RagChunkPO.builder()
                        .ragId(knowledge.getId())
                        .documentId(documentId)
                        .paragraphIndex(chunk.getParagraphIndex())
                        .chunkIndex(chunk.getChunkIndex())
                        .content(chunk.getContent())
                        .tokenCount(chunk.getTokenCount())
                        .contentHash(ContentHashUtil.sha256Hex(chunk.getContent()))
                        .build());
            }

            transactionTemplate.executeWithoutResult(transactionStatus -> {
                ragChunkMapper.insert(chunkPOs);
                // ── Dual write: VectorStore + SearchEngine ──
                dualWrite(chunkPOs, knowledge);

                document.setChunkCount(chunkPOs.size());
                document.setStatus(RagDocumentStatus.SUCCESS.getStatus());
                document.setErrorMsg(null);
                ragDocumentMapper.updateById(document);
            });

            log.info("Document processed successfully: [{}], chunks: {}", documentId, chunkPOs.size());

        } catch (Exception e) {
            log.error("Document processing failed: [{}]", documentId, e);
            updateStatus(documentId, RagDocumentStatus.FAILED, e.getMessage());
        }
    }

    /**
     * Generate vectors for individual slices and write them to the vector library and search engine (New slice scenario)
     */
    public void embedSingleChunk(RagChunkPO chunk, RagPO knowledge) {
        dualWrite(List.of(chunk), knowledge);
    }

    /**
     * Re-embed after deleting the old vector (editing the slice scene)
     */
    public void reEmbedSingleChunk(RagChunkPO chunk, RagPO knowledge) {
        if (StrUtil.isNotBlank(chunk.getVectorId())) {
            try {
                AnikAiVectorStore anikAiVectorStore = vectorStoreFactory.create(knowledge);
                String idx = IndexNameBuilder.KNOWLEDGE.build(Map.of("ragId", knowledge.getId()));
                anikAiVectorStore.delete(idx, List.of(chunk.getVectorId()));
            } catch (Exception e) {
                log.warn("Delete old vector failed for chunk: {}", chunk.getId(), e);
            }
            chunk.setVectorId(null);
        }
        dualWrite(List.of(chunk), knowledge);
    }

    /**
     * Delete the vector corresponding to the slice (called when deleting the slice)
     * The vector is only actually deleted when no other chunk refers to the same vectorId
     */
    public void deleteChunkVector(RagChunkPO chunk) {
        if (StrUtil.isBlank(chunk.getVectorId())) {
            return;
        }
        RagPO knowledge = knowledgeMapper.selectById(chunk.getRagId());
        if (knowledge == null) {
            return;
        }
        // Reference count check: whether there are other chunks sharing the same vectorId
        Long refCount = ragChunkMapper.selectCount(
                new LambdaQueryWrapper<RagChunkPO>()
                        .eq(RagChunkPO::getRagId, chunk.getRagId())
                        .eq(RagChunkPO::getVectorId, chunk.getVectorId())
                        .ne(RagChunkPO::getId, chunk.getId()));
        if (refCount > 0) {
            return;
        }
        try {
            AnikAiVectorStore anikAiVectorStore = vectorStoreFactory.create(knowledge);
            String idx = IndexNameBuilder.KNOWLEDGE.build(Map.of("ragId", knowledge.getId()));
            anikAiVectorStore.delete(idx, List.of(chunk.getVectorId()));
        } catch (Exception e) {
            log.warn("Delete vector failed for chunk: {}", chunk.getId(), e);
        }
    }

    /**
     * Dual-write: embed chunks → write to VectorStore, and insert into SearchEngine.
     */
    private void dualWrite(List<RagChunkPO> chunkPOs, RagPO knowledge) {
        AnikAiVectorStore anikAiVectorStore = vectorStoreFactory.create(knowledge);

        List<List<RagChunkPO>> batches = Lists.partition(chunkPOs, 10);
        for (List<RagChunkPO> batch : batches) {
            processVectorBatch(batch, anikAiVectorStore, knowledge);
        }

        // Write to search engine (if enabled)
        if (Boolean.TRUE.equals(knowledge.getSearchEngineEnable())) {
            try {
                SearchEngine searchEngine = searchEngineFactory.forStoreInstance(knowledge.getSearchEngineInstanceId());
                String searchIndexName = IndexNameBuilder.KNOWLEDGE.build(Map.of("ragId", knowledge.getId()));

                List<SearchDocument> searchDocs = chunkPOs.stream()
                        .map(c -> SearchDocument.builder()
                                .id(String.valueOf(c.getId()))
                                .content(c.getContent())
                                .metadata(Map.of(
                                        SearchMetadataKeys.RAG_ID, knowledge.getId(),
                                        SearchMetadataKeys.DOCUMENT_ID, c.getDocumentId(),
                                        SearchMetadataKeys.CHUNK_ID, c.getId()))
                                .build())
                        .toList();
                searchEngine.insert(SearchAddRequest.builder()
                        .indexName(searchIndexName)
                        .documents(searchDocs)
                        .build());
            } catch (Exception e) {
                log.warn("SearchEngine insert failed (non-fatal): {}", e.getMessage());
            }
        }
    }

    private void processVectorBatch(List<RagChunkPO> batch, AnikAiVectorStore anikAiVectorStore, RagPO knowledge) {
        List<VectorDocument> toEmbed = new ArrayList<>();

        for (RagChunkPO chunk : batch) {
            // Chunk-level deduplication: Find vectorized chunks with the same content under the same RAG
            if (StrUtil.isNotBlank(chunk.getContentHash())) {
                RagChunkPO existing = ragChunkMapper.selectOne(
                        new LambdaQueryWrapper<RagChunkPO>()
                                .eq(RagChunkPO::getRagId, knowledge.getId())
                                .eq(RagChunkPO::getContentHash, chunk.getContentHash())
                                .isNotNull(RagChunkPO::getVectorId)
                                .ne(RagChunkPO::getId, chunk.getId())
                                .last("LIMIT 1"));
                if (existing != null) {
                    // Reuse existing vectors without repeating embedding
                    chunk.setVectorId(existing.getVectorId());
                    ragChunkMapper.updateById(chunk);
                    continue;
                }
            }

            // Need to create a new vector
            String vectorId = UUID.randomUUID().toString();
            chunk.setVectorId(vectorId);

            toEmbed.add(VectorDocument.builder()
                    .id(vectorId)
                    .content(chunk.getContent())
                    .metadata(Map.of(
                            "ragId", knowledge.getId(),
                            "documentId", chunk.getDocumentId(),
                            "chunkId", chunk.getId()))
                    .build());
            ragChunkMapper.updateById(chunk);
        }

        if (!toEmbed.isEmpty()) {
            String indexName = IndexNameBuilder.KNOWLEDGE.build(Map.of("ragId", knowledge.getId()));
            anikAiVectorStore.add(VectorAddRequest.builder()
                    .indexName(indexName)
                    .documents(toEmbed)
                    .build());
        }
    }

    private String parseContent(RagDocumentPO document) {
        InputStream is = resourceService.load(document.getResourceId());
        if (is == null) {
            return "";
        }
        DocumentParser parser = parserFactory.getParser(document.getFileType());
        try (is) {
            return parser.parse(is);
        } catch (Exception e) {
            throw new AnikAiException("Failed to parse document file: " + document.getId(), e);
        }
    }

    private void cleanExistingData(RagPO knowledge, Long documentId) {
        try {
            AnikAiVectorStore anikAiVectorStore = vectorStoreFactory.create(knowledge);
            String indexName = IndexNameBuilder.KNOWLEDGE.build(Map.of("ragId", knowledge.getId()));
            List<RagChunkPO> existing = ragChunkMapper.selectList(
                    new LambdaQueryWrapper<RagChunkPO>()
                            .eq(RagChunkPO::getDocumentId, documentId)
                            .eq(RagChunkPO::getRagId, knowledge.getId())
                            .isNotNull(RagChunkPO::getVectorId));
            if (!existing.isEmpty()) {
                //Only delete vectors that are not referenced by chunks of other documents
                List<String> vectorIdsToDelete = new ArrayList<>();
                for (RagChunkPO chunk : existing) {
                    if (StrUtil.isBlank(chunk.getVectorId())) {
                        continue;
                    }
                    Long refCount = ragChunkMapper.selectCount(
                            new LambdaQueryWrapper<RagChunkPO>()
                                    .eq(RagChunkPO::getRagId, knowledge.getId())
                                    .eq(RagChunkPO::getVectorId, chunk.getVectorId())
                                    .ne(RagChunkPO::getDocumentId, documentId));
                    if (refCount == 0) {
                        vectorIdsToDelete.add(chunk.getVectorId());
                    }
                }
                if (!vectorIdsToDelete.isEmpty()) {
                    anikAiVectorStore.delete(indexName, vectorIdsToDelete);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to clean vector store for document: {}", documentId, e);
        }
        ragChunkMapper.delete(new LambdaQueryWrapper<RagChunkPO>()
                .eq(RagChunkPO::getDocumentId, documentId)
                .eq(RagChunkPO::getRagId, knowledge.getId()));
    }

    private void updateStatus(Long documentId, RagDocumentStatus status, String errorMsg) {
        RagDocumentPO update = new RagDocumentPO();
        update.setId(documentId);
        update.setStatus(status.getStatus());
        update.setErrorMsg(errorMsg);
        ragDocumentMapper.updateById(update);
    }

}

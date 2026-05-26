package com.aizuda.anik.ai.vector.storage.vector.core;

import com.aizuda.anik.ai.model.model.embedding.AnikEmbeddingModel;
import com.aizuda.anik.ai.vector.storage.exception.VectorStoreException;
import com.aizuda.anik.ai.vector.storage.vector.api.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.*;

/**
 * Vector storage abstraction: writing and retrieval are delegated to Spring AI {@code VectorStore} (cached by indexName by subclasses),
 * No embedding is done at this layer; vectorization is done internally by Spring AI in add / similaritySearch.
 */
public abstract class AbstractAnikAiVectorStore implements AnikAiVectorStore {

    protected final AnikEmbeddingModel anikEmbeddingModel;
    protected final org.springframework.ai.embedding.EmbeddingModel springAiEmbeddingModel;
    protected final Integer embeddingDimensions;

    protected AbstractAnikAiVectorStore(AnikEmbeddingModel anikEmbeddingModel,
                                         Integer embeddingDimensions) {
        this.anikEmbeddingModel = anikEmbeddingModel;
        this.embeddingDimensions = embeddingDimensions;
        if (Objects.isNull(anikEmbeddingModel)) {
            springAiEmbeddingModel = null;
            return;
        }
        springAiEmbeddingModel = anikEmbeddingModel.toSpringAiEmbeddingModel();
    }

    @Override
    public void add(VectorAddRequest request) {
        if (request == null || request.getDocuments() == null || request.getDocuments().isEmpty()) {
            return;
        }
        if (request.getIndexName() == null || request.getIndexName().isBlank()) {
            throw new VectorStoreException("indexName cannot be empty");
        }

        List<Document> documents = toDocuments(request.getDocuments());
        getVectorStore(request.getIndexName()).add(documents);
    }

    @Override
    public void delete(String indexName, List<String> ids) {
        getVectorStore(indexName).delete(ids);
    }

    @Override
    public List<VectorSearchResult> search(VectorSearchRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        if (request.getIndexName() == null || request.getIndexName().isBlank()) {
            throw new VectorStoreException("indexName cannot be empty");
        }
        if (request.getQueryText() == null || request.getQueryText().isBlank()) {
            return Collections.emptyList();
        }

        SearchRequest.Builder sb = SearchRequest.builder()
                .query(request.getQueryText())
                .filterExpression(request.getFilterExpression())
                .topK(request.getTopK());
        List<Document> hits = getVectorStore(request.getIndexName()).similaritySearch(sb.build());
        List<VectorSearchResult> results = new ArrayList<>(hits.size());
        for (Document doc : hits) {
            Map<String, Object> meta = new HashMap<>(doc.getMetadata());
            float score = doc.getScore() != null ? doc.getScore().floatValue() : 0f;
            results.add(VectorSearchResult.builder()
                    .id(doc.getId())
                    .content(doc.getText())
                    .score(score)
                    .metadata(meta)
                    .build());
        }
        return results;
    }

    protected abstract VectorStore getVectorStore(String indexName) ;

    private static List<Document> toDocuments(List<VectorDocument> documents) {
        List<Document> out = new ArrayList<>(documents.size());
        for (VectorDocument vd : documents) {
            Map<String, Object> meta = new HashMap<>();
            if (vd.getMetadata() != null) {
                meta.putAll(vd.getMetadata());
            }
            out.add(Document.builder()
                    .id(vd.getId())
                    .text(vd.getContent() != null ? vd.getContent() : "")
                    .metadata(meta)
                    .build());
        }
        return out;
    }
}

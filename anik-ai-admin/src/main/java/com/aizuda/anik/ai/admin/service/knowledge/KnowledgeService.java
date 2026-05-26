package com.aizuda.anik.ai.admin.service.knowledge;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.execption.AnikAiCommonException;
import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.vector.storage.vector.api.AnikAiVectorStore;
import com.aizuda.anik.ai.persistence.rag.mapper.RagMapper;
import com.aizuda.anik.ai.persistence.rag.mapper.RagChunkMapper;
import com.aizuda.anik.ai.persistence.rag.mapper.RagDocumentMapper;
import com.aizuda.anik.ai.persistence.admin.mapper.StoreInstanceMapper;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.persistence.rag.po.RagPO;
import com.aizuda.anik.ai.persistence.rag.po.RagChunkPO;
import com.aizuda.anik.ai.persistence.rag.po.RagDocumentPO;
import com.aizuda.anik.ai.admin.vo.knowledge.KnowledgeConfigRequestVO;
import com.aizuda.anik.ai.persistence.rag.dataobject.RagConfigDO;
import com.aizuda.anik.ai.admin.vo.knowledge.KnowledgeQueryVO;
import com.aizuda.anik.ai.admin.service.VectorDimensionConstraintService;
import com.aizuda.anik.ai.admin.service.model.AiModelConfigService;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigVO;
import com.aizuda.anik.ai.admin.vo.knowledge.KnowledgeRequestVO;
import com.aizuda.anik.ai.admin.vo.knowledge.KnowledgeResponseVO;
import com.aizuda.anik.ai.features.rag.enums.ChunkModeEnum;
import com.aizuda.anik.ai.features.rag.enums.DedupAction;
import com.aizuda.anik.ai.features.rag.enums.DedupStrategy;
import com.aizuda.anik.ai.common.enums.store.StoreCategoryEnum;
import com.aizuda.anik.ai.persistence.admin.po.StoreInstancePO;
import com.aizuda.anik.ai.vector.storage.vector.VectorStoreFactory;
import com.aizuda.anik.ai.vector.storage.vector.api.IndexNameBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final RagMapper knowledgeMapper;
    private final RagDocumentMapper ragDocumentMapper;
    private final RagChunkMapper ragChunkMapper;
    private final StoreInstanceMapper storeInstanceMapper;
    private final AiModelConfigService aiModelConfigService;
    private final VectorDimensionConstraintService vectorDimensionConstraintService;
    private final VectorStoreFactory vectorStoreFactory;

    public KnowledgeResponseVO create(KnowledgeRequestVO request) {
        validateKnowledgeStorage(request);
        validateDimension(request);
        RagConfigDO chunkCfg = buildChunkConfigForCreate(request);
        validateMergedChunk(chunkCfg);
        Long vectorInst = request.getVectorStoreInstanceId();
        Long searchInst = request.getSearchEngineInstanceId();
        RagPO po = RagPO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .vectorStoreInstanceId(vectorInst)
                .dimensionOfVectorModel(request.getDimensionOfVectorModel())
                .embeddingModelId(request.getEmbeddingModelId())
                .rerankModelId(request.getRerankModelId())
                .searchEngineEnable(request.getSearchEngineEnable() != null && request.getSearchEngineEnable())
                .searchEngineInstanceId(
                        Boolean.TRUE.equals(request.getSearchEngineEnable()) ? searchInst : null)
                .delimiter(resolveDelimiterFromChunkConfig(chunkCfg))
                .ragEnhancement(request.getRagEnhancement())
                .config(JsonUtil.toJsonString(chunkCfg))
                .dedupStrategy(DedupStrategy.fromCode(request.getDedupStrategy()).getCode())
                .dedupAction(DedupAction.fromCode(request.getDedupAction()).getCode())
                .uploadConfirm(request.getUploadConfirm() == null ? Boolean.TRUE : request.getUploadConfirm())
                .build();
        knowledgeMapper.insert(po);
        return toResponseVO(po);
    }

    public KnowledgeResponseVO update(Long id, KnowledgeRequestVO request) {
        RagPO po = knowledgeMapper.selectById(id);
        if (po == null) {
            throw new AnikAiException("Knowledge not found: " + id);
        }

        validateKnowledgeStorage(request);
        validateDimension(request);
        po.setName(request.getName());
        po.setDescription(request.getDescription());
        po.setIcon(request.getIcon());
        po.setVectorStoreInstanceId(request.getVectorStoreInstanceId());
        po.setDimensionOfVectorModel(request.getDimensionOfVectorModel());
        po.setEmbeddingModelId(request.getEmbeddingModelId());
        po.setRerankModelId(request.getRerankModelId());
        po.setSearchEngineEnable(request.getSearchEngineEnable() != null && request.getSearchEngineEnable());
        po.setSearchEngineInstanceId(resolveSearchEngineInstanceIdForUpdate(request, po));
        po.setRagEnhancement(request.getRagEnhancement());
        if (request.getDedupStrategy() != null) {
            po.setDedupStrategy(DedupStrategy.fromCode(request.getDedupStrategy()).getCode());
        }
        if (request.getDedupAction() != null) {
            po.setDedupAction(DedupAction.fromCode(request.getDedupAction()).getCode());
        }
        if (request.getUploadConfirm() != null) {
            po.setUploadConfirm(request.getUploadConfirm());
        }

        RagConfigDO merged = mergeChunkConfig(request, po);
        validateMergedChunk(merged);
        po.setConfig(JsonUtil.toJsonString(merged));
        po.setDelimiter(resolveDelimiterFromChunkConfig(merged));

        knowledgeMapper.updateById(po);

        KnowledgeResponseVO vo = toResponseVO(po);
        enrichEmbeddingModelName(vo);
        return vo;
    }

    public void updateConfig(Long id, KnowledgeConfigRequestVO config) {
        RagPO po = knowledgeMapper.selectById(id);
        if (po == null) {
            throw new AnikAiException("Knowledge not found: " + id);
        }
        RagConfigDO existing = parseConfigFromPo(po);
        RagConfigDO incoming = toConfigDO(config);
        if (incoming.getChunkParams() == null) {
            incoming.setChunkParams(existing.getChunkParams());
        }
        if (incoming.getSearchParams() == null) {
            incoming.setSearchParams(existing.getSearchParams());
        }
        if (incoming.getModelParams() == null) {
            incoming.setModelParams(existing.getModelParams());
        }
        po.setConfig(JsonUtil.toJsonString(incoming));
        knowledgeMapper.updateById(po);
    }

    @Transactional
    public void delete(Long id) {
        RagPO po = knowledgeMapper.selectById(id);
        if (po == null) {
            return;
        }

        try {
            AnikAiVectorStore anikAiVectorStore = vectorStoreFactory.create(po);
            anikAiVectorStore.deleteByIndexName(IndexNameBuilder.KNOWLEDGE.build(Map.of("ragId", id)));
        } catch (Exception e) {
            log.warn("Failed to clean vector store for knowledge: {}", id, e);
        }

        ragChunkMapper.delete(new LambdaQueryWrapper<RagChunkPO>().eq(RagChunkPO::getRagId, id));
        ragDocumentMapper.delete(new LambdaQueryWrapper<RagDocumentPO>().eq(RagDocumentPO::getRagId, id));
        knowledgeMapper.deleteById(id);
    }

    public KnowledgeResponseVO getById(Long id) {
        RagPO po = knowledgeMapper.selectById(id);
        if (po == null) {
            throw new AnikAiException("Knowledge not found: " + id);
        }
        KnowledgeResponseVO vo = toResponseVO(po);
        enrichCounts(vo);
        enrichEmbeddingModelName(vo);
        return vo;
    }

    public PageResult<List<KnowledgeResponseVO>> page(KnowledgeQueryVO query) {
        LambdaQueryWrapper<RagPO> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getName())) {
            wrapper.like(RagPO::getName, query.getName());
        }
        wrapper.orderByDesc(RagPO::getCreateDt);

        PageDTO<RagPO> pageDTO = new PageDTO<>(query.getPage(), query.getSize());
        IPage<RagPO> page = knowledgeMapper.selectPage(pageDTO , wrapper);

        IPage<KnowledgeResponseVO> convert = page.convert(po -> {
            KnowledgeResponseVO vo = toResponseVO(po);
            enrichCounts(vo);
            enrichEmbeddingModelName(vo);
            return vo;
        });
        return new PageResult<>(pageDTO, convert.getRecords());
    }

    private void enrichCounts(KnowledgeResponseVO vo) {
        Long docCount = ragDocumentMapper.selectCount(
                new LambdaQueryWrapper<RagDocumentPO>().eq(RagDocumentPO::getRagId, vo.getId()));
        Long chunkCount = ragChunkMapper.selectCount(
                new LambdaQueryWrapper<RagChunkPO>().eq(RagChunkPO::getRagId, vo.getId()));
        vo.setDocumentCount(docCount.intValue());
        vo.setChunkCount(chunkCount.intValue());
    }

    private void enrichEmbeddingModelName(KnowledgeResponseVO vo) {
        if (vo == null || vo.getEmbeddingModelId() == null) {
            return;
        }
        try {
            long id = vo.getEmbeddingModelId();
            AiModelConfigVO m = aiModelConfigService.getModelConfig(id);
            if (m != null && StrUtil.isNotBlank(m.getModelName())) {
                vo.setEmbeddingModelName(m.getModelName());
            }
        } catch (NumberFormatException ignored) {
            // Leave display name blank if it is a non-numeric ID
        }
    }

    private KnowledgeResponseVO toResponseVO(RagPO po) {
        RagConfigDO configDO = StrUtil.isNotBlank(po.getConfig())
                ? JsonUtil.parseObject(po.getConfig(), RagConfigDO.class)
                : null;
        return KnowledgeResponseVO.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .icon(po.getIcon())
                .vectorStoreInstanceId(po.getVectorStoreInstanceId())
                .dimensionOfVectorModel(po.getDimensionOfVectorModel())
                .embeddingModelId(po.getEmbeddingModelId())
                .rerankModelId(po.getRerankModelId())
                .searchEngineEnable(po.getSearchEngineEnable())
                .searchEngineInstanceId(po.getSearchEngineInstanceId())
                .delimiter(po.getDelimiter())
                .ragEnhancement(po.getRagEnhancement())
                .config(toConfigVO(configDO))
                .dedupStrategy(po.getDedupStrategy())
                .dedupAction(po.getDedupAction())
                .uploadConfirm(po.getUploadConfirm())
                .createDt(po.getCreateDt())
                .updateDt(po.getUpdateDt())
                .build();
    }

    private void validateKnowledgeStorage(KnowledgeRequestVO request) {
        if (request.getVectorStoreInstanceId() == null) {
            throw new AnikAiCommonException("Please select a vector library instance");
        }
        StoreInstancePO inst = storeInstanceMapper.selectById(request.getVectorStoreInstanceId());
        if (inst == null || !StoreCategoryEnum.VECTOR_STORE.getCategory().equals(inst.getCategory())) {
            throw new AnikAiCommonException("Invalid vector library instance");
        }
        if (request.getSearchEngineInstanceId() != null) {
            StoreInstancePO se = storeInstanceMapper.selectById(request.getSearchEngineInstanceId());
            if (se == null || !StoreCategoryEnum.SEARCH_ENGINE.getCategory().equals(se.getCategory())) {
                throw new AnikAiCommonException("Invalid search engine instance");
            }
        }
    }

    private void validateDimension(KnowledgeRequestVO request) {
        vectorDimensionConstraintService.validateRequestedDimension(
                request.getDimensionOfVectorModel(),
                request.getEmbeddingModelId(),
                request.getVectorStoreInstanceId());
    }

    private Long resolveSearchEngineInstanceIdForUpdate(KnowledgeRequestVO request, RagPO existing) {
        if (!Boolean.TRUE.equals(request.getSearchEngineEnable())) {
            return null;
        }
        return request.getSearchEngineInstanceId();
    }

    private RagConfigDO parseConfigFromPo(RagPO po) {
        if (StrUtil.isBlank(po.getConfig())) {
            return new RagConfigDO();
        }
        RagConfigDO cfg = JsonUtil.parseObject(po.getConfig(), RagConfigDO.class);
        return cfg != null ? cfg : new RagConfigDO();
    }

    /**
     * Create a new knowledge base: write chunkParams; the delimiter field is aligned with the chunk policy (for old logic to read)
     */
    private RagConfigDO buildChunkConfigForCreate(KnowledgeRequestVO request) {
        RagConfigDO cfg = new RagConfigDO();
        String mode = ChunkModeEnum.fromValue(StrUtil.blankToDefault(request.getChunkMode(), "default")).getMode();
        String customDel = StrUtil.trimToNull(request.getCustomDelimiter());
        if (StrUtil.isNotBlank(request.getDelimiter()) && request.getChunkMode() == null) {
            mode = ChunkModeEnum.DELIMITER.getMode();
            customDel = StrUtil.trimToNull(request.getDelimiter());
        }
        int maxTokens = request.getMaxChunkTokens() != null ? request.getMaxChunkTokens() : 2000;
        Integer overlap = request.getChunkOverlap();
        RagConfigDO.ChunkParams cp = RagConfigDO.ChunkParams.builder()
                .mode(mode)
                .maxChunkTokens(maxTokens)
                .chunkOverlap(overlap)
                .customDelimiter(customDel)
                .chunkRegex(StrUtil.trimToNull(request.getChunkRegex()))
                .chunkModelId(request.getChunkModelId())
                .mergeShortSegments(request.getMergeShortSegments() != null ? request.getMergeShortSegments() : Boolean.TRUE)
                .imageOcr(request.getImageOcr() != null ? request.getImageOcr() : Boolean.FALSE)
                .build();
        cfg.setChunkParams(cp);
        return cfg;
    }

    private void validateMergedChunk(RagConfigDO cfg) {
        if (cfg == null || cfg.getChunkParams() == null) {
            return;
        }
        RagConfigDO.ChunkParams cp = cfg.getChunkParams();
        ChunkModeEnum mode = ChunkModeEnum.fromValue(cp.getMode());
        if (mode == ChunkModeEnum.REGEX) {
            String rx = StrUtil.trimToNull(cp.getChunkRegex());
            if (rx == null) {
                throw new IllegalArgumentException("When slicing according to regular expression, you must fill in the regular expression");
            }
            try {
                Pattern.compile(rx);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regular expression: " + e.getMessage());
            }
        }
        if (mode == ChunkModeEnum.SMART) {
            if (cp.getChunkModelId() == null) {
                throw new IllegalArgumentException("Smart slicing must select a dialogue model");
            }
        }
    }

    private RagConfigDO mergeChunkConfig(KnowledgeRequestVO request, RagPO po) {
        RagConfigDO cfg = parseConfigFromPo(po);
        RagConfigDO.ChunkParams old = cfg.getChunkParams();
        if (old == null) {
            old = new RagConfigDO.ChunkParams();
        }
        String mode = request.getChunkMode() != null
                ? ChunkModeEnum.fromValue(request.getChunkMode()).getMode()
                : ChunkModeEnum.fromValue(StrUtil.blankToDefault(old.getMode(), "default")).getMode();
        Integer max = request.getMaxChunkTokens() != null ? request.getMaxChunkTokens() : old.getMaxChunkTokens();
        if (max == null) {
            max = 2000;
        }
        Integer overlap = request.getChunkOverlap() != null ? request.getChunkOverlap() : old.getChunkOverlap();
        String customDel = request.getCustomDelimiter() != null ? request.getCustomDelimiter() : old.getCustomDelimiter();
        if (StrUtil.isNotBlank(request.getDelimiter()) && request.getChunkMode() == null) {
            mode = ChunkModeEnum.DELIMITER.getMode();
            customDel = request.getDelimiter();
        }
        Boolean mergeShort = request.getMergeShortSegments() != null ? request.getMergeShortSegments() : old.getMergeShortSegments();
        if (mergeShort == null) {
            mergeShort = Boolean.TRUE;
        }
        Boolean imageOcr = request.getImageOcr() != null ? request.getImageOcr() : old.getImageOcr();
        if (imageOcr == null) {
            imageOcr = Boolean.FALSE;
        }
        String chunkRx = request.getChunkRegex() != null ? StrUtil.trimToNull(request.getChunkRegex()) : old.getChunkRegex();
        Long chunkMid = request.getChunkModelId() != null ? request.getChunkModelId() : old.getChunkModelId();
        ChunkModeEnum modeEnum = ChunkModeEnum.fromValue(mode);
        if (modeEnum != ChunkModeEnum.REGEX) {
            chunkRx = null;
        }
        if (modeEnum != ChunkModeEnum.SMART) {
            chunkMid = null;
        }
        RagConfigDO.ChunkParams cp = RagConfigDO.ChunkParams.builder()
                .mode(mode)
                .maxChunkTokens(max)
                .chunkOverlap(overlap)
                .customDelimiter(customDel)
                .chunkRegex(chunkRx)
                .chunkModelId(chunkMid)
                .mergeShortSegments(mergeShort)
                .imageOcr(imageOcr)
                .build();
        cfg.setChunkParams(cp);
        return cfg;
    }

    /**
     * Write PO.delimiter: the default mode is null; single delimiter write library; multiple delimiters (JSON array) only save config to avoid insufficient column length
     */
    private String resolveDelimiterFromChunkConfig(RagConfigDO cfg) {
        if (cfg == null || cfg.getChunkParams() == null) {
            return null;
        }
        ChunkModeEnum mode = ChunkModeEnum.fromValue(cfg.getChunkParams().getMode());
        if (mode != ChunkModeEnum.DELIMITER) {
            return null;
        }
        String cd = StrUtil.trimToNull(cfg.getChunkParams().getCustomDelimiter());
        if (cd == null) {
            return null;
        }
        if (cd.startsWith("[")) {
            return null;
        }
        return StrUtil.blankToDefault(cd, "\n\n");
    }

    private KnowledgeConfigRequestVO toConfigVO(RagConfigDO configDO) {
        if (configDO == null) {
            return null;
        }
        return JsonUtil.parseObject(JsonUtil.toJsonString(configDO), KnowledgeConfigRequestVO.class);
    }

    private RagConfigDO toConfigDO(KnowledgeConfigRequestVO configVO) {
        if (configVO == null) {
            return new RagConfigDO();
        }
        return JsonUtil.parseObject(JsonUtil.toJsonString(configVO), RagConfigDO.class);
    }
}

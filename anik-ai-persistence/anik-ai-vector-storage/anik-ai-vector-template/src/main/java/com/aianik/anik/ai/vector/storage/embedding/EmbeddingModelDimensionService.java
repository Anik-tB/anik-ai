package com.aianik.anik.ai.vector.storage.embedding;

import com.aianik.anik.ai.common.model.ConfigExtAttrsDTO;
import com.aianik.anik.ai.common.util.JsonUtil;
import tools.jackson.core.type.TypeReference;
import com.aianik.anik.ai.model.model.ModelFactory;
import com.aianik.anik.ai.model.model.embedding.AnikEmbeddingModel;
import com.aianik.anik.ai.common.model.embedding.AnikEmbeddingResponse;
import com.aianik.anik.ai.persistence.model.mapper.AiModelConfigMapper;
import com.aianik.anik.ai.persistence.model.po.AiModelConfigPO;
import com.aianik.anik.ai.vector.storage.exception.VectorStoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parse the vector dimensions from the model configuration (and via an embedding call if necessary) and write back {@code config_json.embeddingDimension}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingModelDimensionService {

    private static final int FALLBACK_DIMENSION = 1024;

    private final AiModelConfigMapper modelConfigMapper;
    @Autowired
    @Lazy
    private ModelFactory modelFactory;

    /**
     * Get the embedding model vector dimension: config_json first, then API detection, and finally {@value #FALLBACK_DIMENSION} and persist it.
     */
    public Integer getEmbeddingDimension(Long embeddingModelId) {
        if (embeddingModelId == null) {
            throw new VectorStoreException("embeddingModelId cannot be empty");
        }
        AiModelConfigPO po = modelConfigMapper.selectById(embeddingModelId);
        if (po == null) {
            throw new VectorStoreException("Embedding model does not exist: " + embeddingModelId);
        }
        ConfigExtAttrsDTO attrs = parseAttrs(po);
        if (attrs.getEmbeddingDimension() != null) {
            return attrs.getEmbeddingDimension();
        }
        Integer fromApi = probeDimensionViaApi(embeddingModelId);
        if (fromApi != null) {
            persistDimensionIfAbsent(po, fromApi);
            return fromApi;
        }
        log.warn("Unable to get model dimensions via API, using default value {}: modelId={}", FALLBACK_DIMENSION, embeddingModelId);
        persistDimensionIfAbsent(po, FALLBACK_DIMENSION);
        return FALLBACK_DIMENSION;
    }

    private ConfigExtAttrsDTO parseAttrs(AiModelConfigPO po) {
        ConfigExtAttrsDTO attrs = JsonUtil.parseObject(Optional.ofNullable(po.getConfigJson()).orElse("{}"), ConfigExtAttrsDTO.class);
        return attrs != null ? attrs : new ConfigExtAttrsDTO();
    }

    private Integer probeDimensionViaApi(Long embeddingModelId) {
        try {
            AnikEmbeddingModel model = (AnikEmbeddingModel) modelFactory.getModel(embeddingModelId);
            AnikEmbeddingResponse resp = model.embed(new AnikEmbeddingModel.EmbeddingModelDTO("test", null));
            if (resp == null) {
                return null;
            }
            if (resp.getDimensions() != null) {
                return resp.getDimensions();
            }
            if (resp.getVectors() != null && !resp.getVectors().isEmpty()) {
                float[] v = resp.firstVector();
                return v != null ? v.length : null;
            }
        } catch (Exception e) {
            log.warn("Failed to detect embedding dimensions: modelId={}, {}", embeddingModelId, e.getMessage());
        }
        return null;
    }

    private void persistDimensionIfAbsent(AiModelConfigPO po, int dimension) {
        try {
            ConfigExtAttrsDTO attrs = parseAttrs(po);
            if (attrs.getEmbeddingDimension() != null) {
                return;
            }
            String raw = Optional.ofNullable(po.getConfigJson()).orElse("{}");
            Map<String, Object> map = JsonUtil.parseObject(raw, new TypeReference<Map<String, Object>>() {});
            if (map == null) {
                map = new HashMap<>();
            } else {
                map = new HashMap<>(map);
            }
            map.put("embeddingDimension", dimension);
            po.setConfigJson(JsonUtil.toJsonString(map));
            po.setUpdatedDt(LocalDateTime.now());
            modelConfigMapper.updateById(po);
            log.info("embeddingDimension={} of model {} has been automatically written", po.getId(), dimension);
        } catch (Exception e) {
            log.error("Failed to persist embedding dimension: modelId={}", po.getId(), e);
        }
    }
}

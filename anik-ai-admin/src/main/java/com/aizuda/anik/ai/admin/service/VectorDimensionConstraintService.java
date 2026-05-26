package com.aizuda.anik.ai.admin.service;

import com.aizuda.anik.ai.admin.vo.VectorDimensionConstraintVO;
import com.aizuda.anik.ai.common.enums.rag.StoreInstanceTypeEnum;
import com.aizuda.anik.ai.common.execption.AnikAiCommonException;
import com.aizuda.anik.ai.persistence.admin.mapper.StoreInstanceMapper;
import com.aizuda.anik.ai.persistence.admin.po.StoreInstancePO;
import com.aizuda.anik.ai.vector.storage.embedding.EmbeddingModelDimensionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Unified processing of vector dimension constraints (model upper limit + vector library upper limit).
 */
@Service
@RequiredArgsConstructor
public class VectorDimensionConstraintService {

    private static final int MIN_VECTOR_DIMENSION = 64;
    private static final int DEFAULT_VECTOR_STORE_MAX_DIMENSION = 4096;
    private static final Map<StoreInstanceTypeEnum, Integer> VECTOR_STORE_MAX_DIMENSIONS = Map.of(
            StoreInstanceTypeEnum.PG_VECTOR, 2000,
            StoreInstanceTypeEnum.MILVUS, 4096,
            StoreInstanceTypeEnum.ELASTICSEARCH, 4096
    );

    private final EmbeddingModelDimensionService embeddingModelDimensionService;
    private final StoreInstanceMapper storeInstanceMapper;

    public VectorDimensionConstraintVO resolveConstraint(Long embeddingModelId, Long vectorStoreInstanceId) {
        if (embeddingModelId == null) {
            throw new AnikAiCommonException("embeddingModelId cannot be empty");
        }
        if (vectorStoreInstanceId == null) {
            throw new AnikAiCommonException("vectorStoreInstanceId cannot be empty");
        }
        int modelMaxDimension = getModelMaxDimension(embeddingModelId);
        StoreInstancePO storeInstance = getStoreInstanceOrThrow(vectorStoreInstanceId);
        int storeMaxDimension = getStoreMaxDimension(storeInstance);
        return VectorDimensionConstraintVO.builder()
                .modelMaxDimension(modelMaxDimension)
                .storeMaxDimension(storeMaxDimension)
                .effectiveMaxDimension(Math.min(modelMaxDimension, storeMaxDimension))
                .build();
    }

    public int getModelMaxDimension(Long embeddingModelId) {
        return embeddingModelDimensionService.getEmbeddingDimension(embeddingModelId);
    }

    public void validateRequestedDimension(Integer dimension,
                                           Long embeddingModelId,
                                           Long vectorStoreInstanceId) {
        if (dimension == null) {
            throw new AnikAiCommonException("Vector dimensions are required");
        }
        if (dimension < MIN_VECTOR_DIMENSION) {
            throw new AnikAiCommonException("Vector dimensions cannot be less than" + MIN_VECTOR_DIMENSION);
        }
        int modelMaxDimension = getModelMaxDimension(embeddingModelId);
        if (dimension > modelMaxDimension) {
            throw new AnikAiCommonException("The vector dimension cannot exceed the maximum supported dimension of the model: " + modelMaxDimension);
        }
        StoreInstancePO storeInstance = getStoreInstanceOrThrow(vectorStoreInstanceId);
        int storeMaxDimension = getStoreMaxDimension(storeInstance);
        if (dimension > storeMaxDimension) {
            StoreInstanceTypeEnum storeType = StoreInstanceTypeEnum.fromType(storeInstance.getType());
            String storeDesc = storeType != null ? storeType.getDescription() : "vector library";
            throw new AnikAiCommonException("Vector dimensions cannot exceed" + storeDesc + "Maximum supported dimensions: " + storeMaxDimension);
        }
    }

    private StoreInstancePO getStoreInstanceOrThrow(Long vectorStoreInstanceId) {
        StoreInstancePO storeInstance = storeInstanceMapper.selectById(vectorStoreInstanceId);
        if (storeInstance == null) {
            throw new AnikAiCommonException("Storage instance does not exist");
        }
        return storeInstance;
    }

    private int getStoreMaxDimension(StoreInstancePO storeInstance) {
        StoreInstanceTypeEnum storeType = StoreInstanceTypeEnum.fromType(storeInstance.getType());
        if (storeType == null) {
            return DEFAULT_VECTOR_STORE_MAX_DIMENSION;
        }
        return VECTOR_STORE_MAX_DIMENSIONS.getOrDefault(storeType, DEFAULT_VECTOR_STORE_MAX_DIMENSION);
    }
}

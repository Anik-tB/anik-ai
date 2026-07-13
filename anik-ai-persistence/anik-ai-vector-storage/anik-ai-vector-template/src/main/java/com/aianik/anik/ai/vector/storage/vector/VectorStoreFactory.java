package com.aianik.anik.ai.vector.storage.vector;

import com.aianik.anik.ai.vector.storage.vector.api.AnikAiVectorStore;

import com.aianik.anik.ai.common.enums.rag.StoreInstanceTypeEnum;
import com.aianik.anik.ai.model.model.ModelFactory;
import com.aianik.anik.ai.model.model.embedding.AnikEmbeddingModel;
import com.aianik.anik.ai.persistence.admin.mapper.StoreInstanceMapper;
import com.aianik.anik.ai.vector.storage.enums.VectorStoreType;
import com.aianik.anik.ai.vector.storage.embedding.EmbeddingModelDimensionService;
import com.aianik.anik.ai.persistence.rag.po.RagPO;
import com.aianik.anik.ai.persistence.admin.po.StoreInstancePO;
import com.aianik.anik.ai.vector.storage.exception.VectorStoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * vector library factory: connection and index parameters come from the "storage instance" table config JSON.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VectorStoreFactory {

    public static final Map<VectorStoreType, Function<VectorStoreConfigDTO, AnikAiVectorStore>> REGISTER = new HashMap<>();

    private final ModelFactory modelFactory;
    private final StoreInstanceMapper storeInstanceMapper;
    private final EmbeddingModelDimensionService embeddingModelDimensionService;

    public AnikAiVectorStore create(RagPO knowledge) {
        Integer dimension = knowledge.getDimensionOfVectorModel();
        if (dimension == null) {
            dimension = embeddingModelDimensionService.getEmbeddingDimension(knowledge.getEmbeddingModelId());
        }
        return createForStoreInstance(
                knowledge.getVectorStoreInstanceId(),
                knowledge.getEmbeddingModelId(),
                dimension);
    }


    /**
     * Create a vector library based on storage instance ID + embedded model (shared by knowledge base/memory configuration)
     */
    public AnikAiVectorStore createForStoreInstance(Long vectorStoreInstanceId, Long embeddingModelId, Integer dimensionOfVectorModel) {
        if (vectorStoreInstanceId == null) {
            throw new VectorStoreException("vectorStoreInstanceId cannot be empty");
        }
        StoreInstancePO inst = storeInstanceMapper.selectById(vectorStoreInstanceId);
        if (inst == null) {
            throw new VectorStoreException("Vector library instance does not exist: " + vectorStoreInstanceId);
        }
        StoreInstanceTypeEnum typeEnum = StoreInstanceTypeEnum.fromType(inst.getType());
        if (typeEnum == null) {
            throw new VectorStoreException("Unsupported vector library types: " + inst.getType());
        }
        AnikEmbeddingModel model = (AnikEmbeddingModel) modelFactory.getModel(embeddingModelId);
        return REGISTER.get(VectorStoreType.valueOf(typeEnum.name())).apply(
                VectorStoreConfigDTO
                        .builder()
                        .config(inst.getConfig())
                        .dimensions(dimensionOfVectorModel)
                        .embeddingModel(model)
                        .build()
        );

    }

}

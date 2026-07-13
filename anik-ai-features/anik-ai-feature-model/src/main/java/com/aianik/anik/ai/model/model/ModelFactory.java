package com.aianik.anik.ai.model.model;

import com.aianik.anik.ai.common.model.ModelCallException;
import com.aianik.anik.ai.model.handle.ModelConfigHandler;
import com.aianik.anik.ai.model.dto.ModelConfigInfoDTO;
import com.aianik.anik.ai.model.model.chat.ChatModel;
import com.aianik.anik.ai.model.model.embedding.AnikEmbeddingModel;
import com.aianik.anik.ai.model.model.rerank.RerankModel;
import com.aianik.anik.ai.model.enums.ModelTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * author: openanik
 * date: 2026-03-04
 */
@Component
@RequiredArgsConstructor
public class ModelFactory {
    private final List<ChatModel> chatModels;
    private final List<AnikEmbeddingModel> anikEmbeddingModels;
    private final List<RerankModel> rerankModels;
    private final ModelConfigHandler modelConfigHandler;

    public Model getModel(Long modelConfigId) {
        ModelConfigInfoDTO configInfo = modelConfigHandler.getConfigInfo(modelConfigId);
        String modelType = configInfo.getModelType();

        ModelTypeEnum typeEnum = ModelTypeEnum.fromValue(modelType);
        if (typeEnum == null) {
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Unknown model type: " + modelType);
        }

        Model model = resolveImplementation(typeEnum, configInfo);
        if (model == null) {
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Model implementation not found or type not supported: type=" + modelType + ", modelName=" + configInfo.getModelName());
        }
        model.setModelConfigInfo(configInfo);
        return model;
    }

    private Model resolveImplementation(ModelTypeEnum typeEnum, ModelConfigInfoDTO configInfo) {
        if (typeEnum == ModelTypeEnum.CHAT) {
            return chatModels.stream()
                    .filter(chatModel -> chatModel.supports(configInfo.getModelKey()))
                    .findFirst()
                    .orElse(null);
        }
        if (typeEnum == ModelTypeEnum.EMBEDDING) {
            return anikEmbeddingModels.stream()
                    .filter(embeddingModel -> embeddingModel.supports(configInfo.getModelKey()))
                    .findFirst()
                    .orElse(null);
        }
        if (typeEnum == ModelTypeEnum.RERANKER) {
            return rerankModels.stream()
                    .filter(m -> m.supports(configInfo.getModelKey()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

}

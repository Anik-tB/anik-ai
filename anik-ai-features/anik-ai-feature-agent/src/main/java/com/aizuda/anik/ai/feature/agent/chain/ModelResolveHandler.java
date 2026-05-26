package com.aizuda.anik.ai.feature.agent.chain;


import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.model.dto.ModelConfigInfoDTO;
import com.aizuda.anik.ai.model.enums.ModelTypeEnum;
import com.aizuda.anik.ai.model.handle.ModelConfigHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import com.aizuda.anik.ai.common.dto.agent.ChatDispatchRequest;

import java.io.IOException;
import java.util.Map;

/**
 * Model parsing: Determine the model used by the conversation and prepare the {@link ChatDispatchRequest.ModelConfig} required for remote distribution
 */
@Slf4j
@Component
@Order(30)
@RequiredArgsConstructor
public class ModelResolveHandler implements AgentChatHandler {

    private final ModelConfigHandler modelConfigHandler;

    @Override
    public void handle(AgentChatContext ctx) {
        if (ctx.isTerminated()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        Long modelId = ctx.getAgent().getChatModelId();
        if (modelId == null) {
            ModelConfigInfoDTO model = modelConfigHandler.getDefaultModelByType(ModelTypeEnum.CHAT.getValue());
            if (model == null) {
                try {
                    ctx.getEmitter().send("Error: Dialog model not configured", MediaType.TEXT_PLAIN);
                    ctx.getEmitter().complete();
                } catch (IOException e) {
                    log.error("Failed to write error message", e);
                }
                ctx.setTerminated(true);
                return;
            }
            modelId = model.getId();
        }

        ctx.setModelId(modelId);

        try {
            ModelConfigInfoDTO configInfo = modelConfigHandler.getConfigInfo(modelId);
            ChatDispatchRequest.ModelConfig dispatchModel = ChatDispatchRequest.ModelConfig.builder()
                    .modelKey(configInfo.getModelKey())
                    .apiEndpoint(configInfo.getApiEndpoint())
                    .apiKey(modelConfigHandler.decryptApiKey(configInfo.getEncryptedApiKey()))
                    .configJson(configInfo.getConfigJson())
                    .build();
            ctx.setModelConfig(dispatchModel);
            log.debug("Model config prepared for dispatch: modelName={}", configInfo.getModelName());
        } catch (Exception e) {
            log.error("Failed to prepare model config for remote dispatch, modelId={}", modelId, e);
            try {
                ctx.getEmitter().send("Error: Model configuration failed to load", MediaType.TEXT_PLAIN);
                ctx.getEmitter().complete();
            } catch (IOException ex) {
                log.error("Failed to write error message", ex);
            }
            ctx.setTerminated(true);
        }
    }


}

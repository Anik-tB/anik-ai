package com.aianik.anik.ai.model.model.chat;

import com.aianik.anik.ai.common.log.AnikAiLog;
import com.aianik.anik.ai.common.model.ModelCallException;
import com.aianik.anik.ai.model.builder.ChatClientBuilder;
import com.aianik.anik.ai.model.handle.ModelConfigHandler;
import com.aianik.anik.ai.model.dto.ModelConfigInfoDTO;
import com.aianik.anik.ai.model.model.AbstractModel;
import com.aianik.anik.ai.model.service.McpToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * author: openanik
 * date: 2026-03-04
 */
@Slf4j
@Service()
@RequiredArgsConstructor
@Scope("prototype")
public class DefaultChatModel extends AbstractModel implements ChatModel {
    private final ChatClientBuilder chatClientBuilder;
    private final McpToolService mcpToolService;
    private final ModelConfigHandler modelConfigHandler;

    @Override
    public boolean supports(String modelKey) {
        return true;
    }

    @Override
    public String chatModel(ChatModelDTO chatModelDTO) throws ModelCallException {
        return callModel(chatModelDTO.userContext(), chatModelDTO.systemContext());
    }

    @Override
    public void chatStreamModel(ChatStreamModelDTO chatModelDTO) throws ModelCallException {
        callModelStream(chatModelDTO.userContext(), chatModelDTO.systemContext(),
                chatModelDTO.messageConsumer(), chatModelDTO.onComplete(), chatModelDTO.onError());
    }

    /**
     * Streaming call based on configuration ID
     * Suitable for scenarios that require real-time display of output (such as chat conversations)
     *
     * @param userContext user input content
     * @param systemContext system prompt word
     * @param messageConsumer message consumption callback, this method will be called for each returned message
     * @throws ModelCallException if Configuration does not exist, no permission, call failure, etc.
     */
    public void callModelStream(String userContext,
                                String systemContext,
                                Consumer<String> messageConsumer,
                                Runnable onComplete,
                                Consumer<Throwable> onError)
            throws ModelCallException {
        Long modelConfigId = modelConfigInfo.getId();

        //1. Parameter verification
        validateInputs(modelConfigInfo.getId(), userContext);

        long startTime = System.currentTimeMillis();

        try {
            // 3. Decrypt API Key
            String decryptedApiKey = decryptApiKey(modelConfigInfo);

            // 4. Get or build ChatClient
            ChatClient chatClient = chatClientBuilder.getOrBuildChatClient(
                    decryptedApiKey,
                    modelConfigInfo
            );

            // 5. Build the message
            Prompt prompt = buildPrompt(userContext, systemContext);

            // 6. Streaming call
            AnikAiLog.LOCAL.info("Stream calling model: {}, config: {}",
                    modelConfigInfo.getModelName(), modelConfigId);

            // Build prompt request, inject MCP tool and Skill tool (if any)
            ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(prompt);

            log.debug("prompt:{}", prompt.getSystemMessage());
            requestSpec
                    .stream()
                    .chatResponse()
                    .subscribe(
                            chatResponse -> {
                                //Extract text content
                                String text = java.util.Optional.ofNullable(chatResponse)
                                        .map(org.springframework.ai.chat.model.ChatResponse::getResult)
                                        .map(org.springframework.ai.chat.model.Generation::getOutput)
                                        .map(org.springframework.ai.chat.messages.AbstractMessage::getText)
                                        .orElse(null);
                                if (StringUtils.hasText(text)) {
                                    messageConsumer.accept(text);
                                }
                            },
                            error -> {
                                long duration = System.currentTimeMillis() - startTime;
                                AnikAiLog.LOCAL.error("Stream call failed for model: {}, error: {}",
                                        modelConfigId, error.getMessage(), error);
                                if (onError != null) {
                                    onError.accept(error);
                                }
                            },
                            () -> {
                                // Call completed
                                long duration = System.currentTimeMillis() - startTime;
                                AnikAiLog.LOCAL.info("Stream call completed for model: {}, duration: {}ms",
                                        modelConfigId, duration);
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                    );

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            AnikAiLog.LOCAL.error("Stream call failed for config: {}, error: {}",
                    modelConfigId, e.getMessage(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Model call failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Non-streaming call based on configuration ID
     * Suitable for scenarios that require complete results (such as translation, summary, etc.)
     *
     * @param userContext user input content
     * @param systemContext system prompt word
     * @return the complete response returned by the model
     * @throws ModelCallException if Configuration does not exist, no permission, call failure, etc.
     */
    public String callModel(String userContext,
                            String systemContext)
            throws ModelCallException {

        //1. Parameter verification
//        validateInputs(userContext);

        long startTime = System.currentTimeMillis();

        try {
            // 3. Decrypt API Key
            String decryptedApiKey = decryptApiKey(modelConfigInfo);

            // 4. Get or build ChatClient
            ChatClient chatClient = chatClientBuilder.getOrBuildChatClient(
                    decryptedApiKey,
                    modelConfigInfo
            );

            // 5. Build the message
            Prompt prompt = buildPrompt(userContext, systemContext);

            // 6. Non-streaming calls
            AnikAiLog.LOCAL.info("Calling model: {}, config: {}",
                    modelConfigInfo.getModelName(), modelConfigInfo.getId());

            String result = chatClient.prompt(prompt)
                    .call()
                    .content();

            // 7. Record completion log
            long duration = System.currentTimeMillis() - startTime;

            AnikAiLog.LOCAL.info("Model call completed: {}, duration: {}ms",
                    modelConfigInfo.getId(), duration);

            return result;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            AnikAiLog.LOCAL.error("Model call failed for config: {}, error: {}",
                    modelConfigInfo.getId(), e.getMessage(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Model call failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * parameter verification
     */
    private void validateInputs(Long modelConfigId, String userContext) throws ModelCallException {
        if (modelConfigId == null || modelConfigId <= 0) {
            throw new ModelCallException(ModelCallException.ErrorCode.INVALID_PARAMETER,
                    "Model configuration ID cannot be empty and must be greater than 0");
        }
        if (!StringUtils.hasText(userContext)) {
            throw new ModelCallException(ModelCallException.ErrorCode.INVALID_PARAMETER,
                    "User input cannot be empty");
        }
    }


    /**
     * Decrypt API Key
     */
    private String decryptApiKey(ModelConfigInfoDTO config) throws ModelCallException {
        try {
            // Call the service layer to obtain the decrypted API Key
            String decryptedKey = modelConfigHandler.decryptApiKey(config.getEncryptedApiKey());
            if (!StringUtils.hasText(decryptedKey)) {
                throw new ModelCallException(ModelCallException.ErrorCode.API_KEY_DECRYPT_FAILED,
                        "API Key decryption failed or does not exist");
            }
            return decryptedKey;
        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            AnikAiLog.LOCAL.error("Failed to decrypt API Key for config: {}", config.getId(), e);
            throw new ModelCallException(ModelCallException.ErrorCode.API_KEY_DECRYPT_FAILED,
                    "API Key decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Build Prompt
     */
    private Prompt buildPrompt(String userContext, String systemContext) {
        // Build message list
        List<Message> messages = new ArrayList<>();

        //Add system prompt word
        if (StringUtils.hasText(systemContext)) {
            messages.add(new SystemMessage(systemContext));
        }

        //Add user input
        messages.add(new UserMessage(userContext));

        return new Prompt(messages);
    }


}

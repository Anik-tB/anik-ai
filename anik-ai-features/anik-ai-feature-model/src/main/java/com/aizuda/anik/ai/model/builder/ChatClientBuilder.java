package com.aizuda.anik.ai.model.builder;

import com.aizuda.anik.ai.common.model.ModelCallException;
import com.aizuda.anik.ai.model.dto.ModelConfigInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatClient dynamic builder
 * Dynamically build ChatClient instances at runtime based on Model configuration
 * Supports multiple providers (OpenAI, Claude, etc.)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatClientBuilder {

    private final List<ChatModelFactory> chatModelFactories;
    private final List<StreamAdvisor> anikChatAdvisors;

    /**
     * Get or build ChatClient
     */
    public ChatClient getOrBuildChatClient(String decryptedApiKey, ModelConfigInfoDTO config)
            throws ModelCallException {
        try {
            log.debug("Building ChatClient for config: {}, provider: {}", config.getId(), config.getProviderKey());
            return buildChatClient(config, decryptedApiKey);
        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to build ChatClient for config: {}", config.getId(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.CHAT_CLIENT_BUILD_FAILED,
                    "ChatClient build failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Dynamically build ChatClient based on Model configuration
     */
    public ChatClient buildChatClient(ModelConfigInfoDTO config, String decryptedApiKey)
            throws ModelCallException {
        try {
            ChatModelFactory factory = getChatModelFactory(config.getProviderKey());
            ChatModel chatModel = factory.createChatModel(
                    config.getProviderKey(),
                    config.getApiEndpoint(),
                    decryptedApiKey,
                    config.getModelKey(),
                    config.getConfigJson()
            );

            List<Advisor> advisors = new ArrayList<>();
            advisors.add(new SimpleLoggerAdvisor());
            advisors.addAll(anikChatAdvisors);
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultAdvisors(advisors.toArray(Advisor[]::new))
                    .build();

            log.info("Successfully built ChatClient for config: {} (model: {})", config.getId(), config.getModelKey());
            return chatClient;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to build ChatClient for config: {}", config.getId(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.CHAT_CLIENT_BUILD_FAILED,
                    "ChatClient build failed: " + e.getMessage(),
                    e
            );
        }
    }

    private ChatModelFactory getChatModelFactory(String providerKey) throws ModelCallException {
        ChatModelFactory factory = chatModelFactories.stream().findFirst().orElse(null);
        if (factory == null) {
            throw new ModelCallException(
                    ModelCallException.ErrorCode.PROVIDER_NOT_SUPPORTED,
                    "Unsupported providers: " + providerKey
            );
        }
        return factory;
    }
}

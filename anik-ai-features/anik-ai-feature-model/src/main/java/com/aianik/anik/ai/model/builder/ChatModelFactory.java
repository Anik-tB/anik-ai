package com.aianik.anik.ai.model.builder;

import com.aianik.anik.ai.common.model.ConfigExtAttrsDTO;
import org.springframework.ai.chat.model.ChatModel;

/**
 * ChatModel factory interface - supports ChatModel creation from different providers
 * Every provider (OpenAI, Claude, Ollama, etc.) can implement this interface
 */
public interface ChatModelFactory {

    /**
     * Get the provider ID supported by this factory
     * For example: "openai", "claude", "ollama" etc.
     */
    String getSupportedProvider();

    /**
     * Create ChatModel based on provider and configuration
     *
     * @param providerKey provider ID (such as "openai")
     * @param baseUrl API base URL (such as https://api.openai.com/v1)
     * @param apiKey decrypted API Key
     * @param modelKey model identifier (such as "gpt-4")
     * @param configJson configuration JSON (including parameters such as temperature)
     * @return the created ChatModel
     * @throws Exception if creation fails
     */
    ChatModel createChatModel(String providerKey, String baseUrl, String apiKey,
                             String modelKey, ConfigExtAttrsDTO configJson) throws Exception;

    /**
     * Verify provider configuration is valid
     */
    default boolean isConfigValid(String baseUrl, String apiKey) {
        return baseUrl != null && !baseUrl.isEmpty()
                && apiKey != null && !apiKey.isEmpty();
    }
}

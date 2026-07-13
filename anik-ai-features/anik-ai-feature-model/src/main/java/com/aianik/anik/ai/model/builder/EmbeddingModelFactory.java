package com.aianik.anik.ai.model.builder;

import com.aianik.anik.ai.common.model.ConfigExtAttrsDTO;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * AnikEmbeddingModel factory interface - supports AnikEmbeddingModel creation from different providers
 * Every provider (OpenAI, Claude, Ollama, etc.) can implement this interface
 */
public interface EmbeddingModelFactory {

    /**
     * Get the provider ID supported by this factory
     * For example: "openai", "claude", "ollama" etc.
     */
    String getSupportedProvider();

    /**
     * Create AnikEmbeddingModel based on provider and configuration
     *
     * @param providerKey provider ID (such as "openai")
     * @param baseUrl API base URL (such as https://api.openai.com/v1)
     * @param apiKey decrypted API Key
     * @param modelKey model identifier (such as "text-embedding-3-large")
     * @param configJson configuration JSON (including parameters such as dimensions)
     * @return the created AnikEmbeddingModel
     * @throws Exception if creation fails
     */
    EmbeddingModel createEmbeddingModel(String providerKey, String baseUrl, String apiKey,
                                       String modelKey, ConfigExtAttrsDTO configJson) throws Exception;

    /**
     * Verify provider configuration is valid
     */
    default boolean isConfigValid(String baseUrl, String apiKey) {
        return baseUrl != null && !baseUrl.isEmpty()
                && apiKey != null && !apiKey.isEmpty();
    }
}

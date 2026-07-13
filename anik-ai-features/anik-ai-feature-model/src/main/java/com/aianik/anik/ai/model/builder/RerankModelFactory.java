package com.aianik.anik.ai.model.builder;

import com.aianik.anik.ai.common.model.ConfigExtAttrsDTO;
import com.aianik.anik.ai.common.model.RerankApiClient;

/**
 * RerankModel factory interface - supports Rerank client creation from different providers
 */
public interface RerankModelFactory {

    /**
     * Get the provider ID supported by this factory
     */
    String getSupportedProvider();

    /**
     * Create Rerank API client based on provider and configuration
     *
     * @param providerKey provider ID
     * @param baseUrl API base URL
     * @param apiKey decrypted API Key
     * @param modelKey model identifier
     * @param configJson configuration JSON
     * @return Rerank API client
     */
    RerankApiClient createRerankClient(String providerKey, String baseUrl, String apiKey,
                                       String modelKey, ConfigExtAttrsDTO configJson) throws Exception;

    default boolean isConfigValid(String baseUrl, String apiKey) {
        return baseUrl != null && !baseUrl.isEmpty()
                && apiKey != null && !apiKey.isEmpty();
    }
}

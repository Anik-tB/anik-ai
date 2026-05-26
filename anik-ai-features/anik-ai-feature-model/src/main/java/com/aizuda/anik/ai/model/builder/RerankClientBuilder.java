package com.aizuda.anik.ai.model.builder;

import com.aizuda.anik.ai.common.model.ModelCallException;
import com.aizuda.anik.ai.common.model.RerankApiClient;
import com.aizuda.anik.ai.model.dto.ModelConfigInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rerank client-side dynamic builder
 * Refer to EmbeddingClientBuilder design
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RerankClientBuilder {

    private final List<RerankModelFactory> rerankModelFactories;

    /**
     * Build RerankApiClient
     *
     * @param decryptedApiKey decrypted API Key
     * @param config model configuration information
     * @return RerankApiClient instance
     */
    public RerankApiClient buildRerankClient(String decryptedApiKey, ModelConfigInfoDTO config)
            throws ModelCallException {
        try {
            RerankModelFactory factory = getRerankModelFactory(config.getProviderKey());

            RerankApiClient client = factory.createRerankClient(
                    config.getProviderKey(),
                    config.getApiEndpoint(),
                    decryptedApiKey,
                    config.getModelKey(),
                    config.getConfigJson()
            );

            log.info("Successfully built RerankApiClient for config: {} (model: {})",
                    config.getId(), config.getModelKey());
            return client;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to build RerankApiClient for config: {}", config.getId(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.CHAT_MODEL_BUILD_FAILED,
                    "RerankClient build failed: " + e.getMessage(), e);
        }
    }

    private RerankModelFactory getRerankModelFactory(String providerKey) throws ModelCallException {
        if (rerankModelFactories == null || rerankModelFactories.isEmpty()) {
            throw new ModelCallException(
                    ModelCallException.ErrorCode.PROVIDER_NOT_SUPPORTED,
                    "No Rerank factories are registered");
        }
        String key = providerKey != null ? providerKey.trim() : "";
        RerankModelFactory factory = rerankModelFactories.stream()
                .filter(f -> key.equalsIgnoreCase(f.getSupportedProvider()))
                .findFirst()
                .orElse(null);
        if (factory == null) {
            // Most vendors compatible with OpenAI style/rerank HTTP: Fallback to openai implementation
            factory = rerankModelFactories.stream()
                    .filter(f -> "openai".equalsIgnoreCase(f.getSupportedProvider()))
                    .findFirst()
                    .orElse(null);
            if (factory != null) {
                log.warn("No RerankModelFactory for provider [{}], using openai-compatible HTTP client", providerKey);
            }
        }
        if (factory == null) {
            throw new ModelCallException(
                    ModelCallException.ErrorCode.PROVIDER_NOT_SUPPORTED,
                    "Unsupported providers: " + providerKey);
        }
        return factory;
    }
}

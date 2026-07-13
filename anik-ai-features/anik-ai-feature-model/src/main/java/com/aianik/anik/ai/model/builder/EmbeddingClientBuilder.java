package com.aianik.anik.ai.model.builder;

import com.aianik.anik.ai.common.model.ModelCallException;
import com.aianik.anik.ai.model.dto.ModelConfigInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AnikEmbeddingModel dynamic builder
 * Dynamically build AnikEmbeddingModel instances at runtime based on the Model configuration
 * Supports multiple providers (OpenAI, Claude, etc.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingClientBuilder {

    private final List<EmbeddingModelFactory> embeddingModelFactories;
    /**
     * Get or build AnikEmbeddingModel (with cache)
     *
     * @param decryptedApiKey decrypted API Key
     * @param config model configuration information
     * @return AnikEmbeddingModel instance
     * @throws ModelCallException if build fails
     */
    public EmbeddingModel getOrBuildEmbeddingModel(String decryptedApiKey, ModelConfigInfoDTO config)
            throws ModelCallException {

        try {
            // 2. Cache invalidation, dynamic construction
            log.debug("Building AnikEmbeddingModel for config: {}, provider: {}", config.getId(), config.getProviderKey());
            EmbeddingModel embeddingModel = buildEmbeddingModel(config, decryptedApiKey);

            log.debug("AnikEmbeddingModel cached for config: {}", config.getId());

            return embeddingModel;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to build AnikEmbeddingModel for config: {}", config.getId(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.CHAT_MODEL_BUILD_FAILED,
                    "AnikEmbeddingModel build failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Dynamically build AnikEmbeddingModel based on Model configuration
     *
     * @param config model configuration information
     * @param decryptedApiKey decrypted API Key
     * @return AnikEmbeddingModel instance
     * @throws ModelCallException if build fails
     */
    public EmbeddingModel buildEmbeddingModel(ModelConfigInfoDTO config, String decryptedApiKey)
            throws ModelCallException {

        try {
            // 1. Get the AnikEmbeddingModel factory of the corresponding provider
            EmbeddingModelFactory factory = getEmbeddingModelFactory(config.getProviderKey());

            // 2. Create AnikEmbeddingModel through factory
            EmbeddingModel embeddingModel = factory.createEmbeddingModel(
                    config.getProviderKey(),
                    config.getApiEndpoint(),
                    decryptedApiKey,
                    config.getModelKey(),
                    config.getConfigJson()
            );

            log.info("Successfully built AnikEmbeddingModel for config: {} (model: {})", config.getId(), config.getModelKey());

            return embeddingModel;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to build AnikEmbeddingModel for config: {}", config.getId(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.CHAT_MODEL_BUILD_FAILED,
                    "AnikEmbeddingModel build failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Get the AnikEmbeddingModel factory of the corresponding provider
     *
     * @param providerKey provider ID (such as "openai")
     * @return AnikEmbeddingModel factory
     * @throws ModelCallException if the provider does not support
     */
    private EmbeddingModelFactory getEmbeddingModelFactory(String providerKey) throws ModelCallException {
        // todo needs to be adapted here
        EmbeddingModelFactory factory = embeddingModelFactories.stream().findFirst().orElse(null);
        if (factory == null) {
            log.error("Unsupported provider for embedding: {}", providerKey);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.PROVIDER_NOT_SUPPORTED,
                    "Unsupported providers: " + providerKey
            );
        }
        return factory;
    }
}

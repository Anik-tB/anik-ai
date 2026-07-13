package com.aianik.anik.ai.model.model.embedding;

import com.aianik.anik.ai.common.log.AnikAiLog;
import com.aianik.anik.ai.common.model.ModelCallException;
import com.aianik.anik.ai.model.builder.EmbeddingClientBuilder;
import com.aianik.anik.ai.model.handle.ModelConfigHandler;
import com.aianik.anik.ai.model.dto.ModelConfigInfoDTO;
import com.aianik.anik.ai.model.model.AbstractModel;
import com.aianik.anik.ai.common.model.embedding.EmbeddingVector;
import com.aianik.anik.ai.common.model.embedding.AnikEmbeddingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default vector model implementation
 * Provides a unified vectorized interface that supports multiple providers and configurations
 *
 * author: openanik
 * date: 2026-03-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultAnikEmbeddingModel extends AbstractModel implements AnikEmbeddingModel {
    private final ModelConfigHandler modelConfigHandler;
    private final EmbeddingClientBuilder embeddingClientBuilder;

    @Override
    public boolean supports(String modelKey) {
        // All models are supported by default (verified by DynamicEmbeddingCaller)
        return true;
    }

    @Override
    public AnikEmbeddingResponse embed(EmbeddingModelDTO dto) throws ModelCallException {
        log.info("Embedding single text with model config: {}", dto.text());
        return embed(dto.text(), dto.dimensions());
    }

    @Override
    public AnikEmbeddingResponse embedBatch(EmbeddingBatchModelDTO dto) throws ModelCallException {
        log.info("Embedding batch texts with model config: {}", dto.texts());
        return embedBatch(dto.texts(), dto.dimensions());
    }

    @Override
    public org.springframework.ai.embedding.EmbeddingModel toSpringAiEmbeddingModel() {
        try {
            String decryptedApiKey = decryptApiKey(modelConfigInfo);
            return embeddingClientBuilder.getOrBuildEmbeddingModel(decryptedApiKey, modelConfigInfo);
        } catch (ModelCallException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Vectorized calls based on configuration ID
     * Suitable for scenarios that require converting text into vectors (such as search, similarity calculation)
     *
     * @param text text to be vectorized
     * @return vector list
     * @throws ModelCallException if Configuration does not exist, no permission, call failure, etc.
     */
    public AnikEmbeddingResponse embed( String text, Integer dimensions) throws ModelCallException {

        long startTime = System.currentTimeMillis();

        try {
            // 3. Decrypt API Key
            String decryptedApiKey = decryptApiKey(modelConfigInfo);

            // 4. Get or build AnikEmbeddingModel
            org.springframework.ai.embedding.EmbeddingModel embeddingModel = embeddingClientBuilder.getOrBuildEmbeddingModel(
                    decryptedApiKey,
                    modelConfigInfo
            );

            // 5. Perform vectorization
            AnikAiLog.LOCAL.info("Embedding text with model: {}, config: {}",
                    modelConfigInfo.getModelName(), modelConfigInfo.getId());

            EmbeddingRequest request = new EmbeddingRequest(
                    Collections.singletonList(text),
                    new EmbeddingOptions() {
                        @Override
                        public String getModel() {
                            return modelConfigInfo.getModelKey();
                        }

                        @Override
                        public @Nullable Integer getDimensions() {
                            return dimensions;
                        }
                    }
            );
            EmbeddingResponse response = embeddingModel.call(request);
            // 6. Convert the response and log it
            long duration = System.currentTimeMillis() - startTime;
            AnikEmbeddingResponse anikEmbeddingResponse = convertResponse(response, List.of(text), duration);

            AnikAiLog.LOCAL.info("Embedding completed: {}, duration: {}ms",
                    modelConfigInfo.getId(), duration);

            return anikEmbeddingResponse;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            AnikAiLog.LOCAL.error("Embedding failed for config: {}, error: {}",
                    modelConfigInfo.getId(), e.getMessage(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Vectorized call failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Batch vectorized calls based on configuration ID
     * Suitable for scenarios where multiple texts need to be converted into vectors
     *
     * @param texts List of texts to be vectorized
     * @return vector list (corresponding to input order)
     * @throws ModelCallException if Configuration does not exist, no permission, call failure, etc.
     */
    public AnikEmbeddingResponse embedBatch(List<String> texts, Integer dimensions) throws ModelCallException {
        if (texts == null || texts.isEmpty()) {
            throw new ModelCallException(ModelCallException.ErrorCode.INVALID_PARAMETER,
                    "Text list cannot be empty");
        }

        Long userId = getUserId();
        long startTime = System.currentTimeMillis();

        try {

            // 3. Decrypt API Key
            String decryptedApiKey = decryptApiKey(modelConfigInfo);

            // 4. Get or build AnikEmbeddingModel
            org.springframework.ai.embedding.EmbeddingModel embeddingModel = embeddingClientBuilder.getOrBuildEmbeddingModel(
                    decryptedApiKey,
                    modelConfigInfo
            );

            //5. Perform batch vectorization
            AnikAiLog.LOCAL.info("Batch embedding {} texts with model: {}, config: {}, userId: {}",
                    texts.size(), modelConfigInfo.getModelName(), modelConfigInfo.getId(), userId);

            EmbeddingRequest request = new EmbeddingRequest(
                    texts,
                    new EmbeddingOptions() {
                        @Override
                        public String getModel() {
                            return modelConfigInfo.getModelKey();
                        }

                        @Override
                        public @Nullable Integer getDimensions() {
                            return dimensions;
                        }
                    }
            );
            EmbeddingResponse response = embeddingModel.call(request);


            // 6. Convert the response and log it
            long duration = System.currentTimeMillis() - startTime;
            AnikEmbeddingResponse anikEmbeddingResponse = convertResponse(response, texts, duration);

            AnikAiLog.LOCAL.info("Batch embedding completed: {}, textCount: {}, duration: {}ms",
                    modelConfigInfo.getId(), texts.size(), duration);

            return anikEmbeddingResponse;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            AnikAiLog.LOCAL.error("Batch embedding failed for config: {}, error: {}",
                    modelConfigInfo.getId(), e.getMessage(), e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.MODEL_CALL_FAILED,
                    "Batch vectorization call failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Get current userID
     */
    private Long getUserId() {
        return 0L;
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
     * Estimate the number of Tokens (simple rough estimate)
     * In fact, it should be calculated based on the word segmenter of the model.
     * Here is a simple estimate based on the ratio of 1 token ≈ 4 characters.
     */
    private int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        //Rough estimate: Chinese is about 1 character per token, English is about 4 characters per token
        return (int) Math.ceil(text.length() / 3.5);
    }

    /**
     * Convert float array to List<Float>
     */
    private List<Float> convertToFloatList(float[] floatArray) {
        List<Float> result = new ArrayList<>();
        if (floatArray != null) {
            for (float f : floatArray) {
                result.add(f);
            }
        }
        return result;
    }

    private AnikEmbeddingResponse convertResponse(
            EmbeddingResponse embeddingResponse, List<String> texts,
            long cost) {

        AnikEmbeddingResponse response = new AnikEmbeddingResponse();
        response.setCostTimeMs(cost);

        List<EmbeddingVector> vectors = new ArrayList<>();

        for (int i = 0; i < embeddingResponse.getResults().size(); i++) {

            float[] vector =
                    embeddingResponse.getResults().get(i).getOutput();

            EmbeddingVector v = new EmbeddingVector();
            v.setIndex(i);
            v.setInput(texts.get(i));
            v.setVector(vector);

            vectors.add(v);
        }

        response.setVectors(vectors);

        if (!vectors.isEmpty()) {
            response.setDimensions(vectors.get(0).getVector().length);
        }

        return response;
    }
}


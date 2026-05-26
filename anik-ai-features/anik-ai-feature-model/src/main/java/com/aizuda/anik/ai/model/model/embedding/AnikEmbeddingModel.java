package com.aizuda.anik.ai.model.model.embedding;

import com.aizuda.anik.ai.common.model.ModelCallException;
import com.aizuda.anik.ai.model.model.Model;
import com.aizuda.anik.ai.common.model.embedding.AnikEmbeddingResponse;

import java.util.List;

/**
 * vector model interface
 * author: openanik
 * date: 2026-03-04
 */
public interface AnikEmbeddingModel extends Model {

    /**
     * Single text vectorization
     */
    AnikEmbeddingResponse embed(EmbeddingModelDTO dto) throws ModelCallException;

    /**
     * Batch text vectorization
     */
    AnikEmbeddingResponse embedBatch(EmbeddingBatchModelDTO dto) throws ModelCallException;

    /**
     * Single text vectorized request DTO
     *
     * @param text text to be vectorized
     * @param dimensions vector dimensions (optional)
     */
    record EmbeddingModelDTO(String text, Integer dimensions) {
    }

    /**
     * Bulk text vectorization request DTO
     *
     * @param texts List of texts to be vectorized
     * @param dimensions vector dimensions (optional)
     */
    record EmbeddingBatchModelDTO(List<String> texts, Integer dimensions) {
    }

    /**
     * Exposes Spring AI's embedded model for vector libraries to interface with Spring AI VectorStore.
     * Not supported by the default implementation; specific implementation provided by {@link DefaultAnikEmbeddingModel}.
     */
    default org.springframework.ai.embedding.EmbeddingModel toSpringAiEmbeddingModel() {
        throw new UnsupportedOperationException(
                "toSpringAiEmbeddingModel() is only supported by DefaultAnikEmbeddingModel");
    }
}

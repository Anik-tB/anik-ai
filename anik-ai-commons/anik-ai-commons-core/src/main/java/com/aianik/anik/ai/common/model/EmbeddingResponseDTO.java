package com.aianik.anik.ai.common.model;

import lombok.Data;

import java.util.List;

/**
 * (Copied from anik-job-ai-executor)
 */
@Data
public class EmbeddingResponseDTO {

    private Integer totalTokens;
    private List<EmbeddingItem> embeddings;

    @Data
    public static class EmbeddingItem {
        private float[] embedding;
        private Integer index;
    }
}

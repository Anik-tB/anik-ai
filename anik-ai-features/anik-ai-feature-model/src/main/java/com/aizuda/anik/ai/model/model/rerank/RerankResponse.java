package com.aizuda.anik.ai.model.model.rerank;

import lombok.Data;

import java.util.List;

/**
 * rearrange model response
 */
@Data
public class RerankResponse {
    private List<RerankResult> results;
    private long costTimeMs;

    @Data
    public static class RerankResult {
        /** The index of the original document in the input list */
        private int index;
        /** Rearrange scores */
        private double score;
    }
}

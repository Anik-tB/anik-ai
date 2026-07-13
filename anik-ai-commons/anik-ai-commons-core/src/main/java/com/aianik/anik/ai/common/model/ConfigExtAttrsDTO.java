package com.aianik.anik.ai.common.model;

import lombok.Data;

import java.util.List;

/**
 * Model extension configuration properties
 * <p>
 * Grouped by model type:
 * - Common: timeoutMs
 * - CHAT：temperature, topP, topK, maxTokens, frequencyPenalty, presencePenalty, stopSequences, seed, responseFormat, stream, completionsPath
 * - EMBEDDING：embeddingDimension, embeddingsPath, encodingFormat
 * - RERANKER：rerankPath
 *
 * @author openanik
 * @date 2026-03-04
 */
@Data
public class ConfigExtAttrsDTO {

    // ==================== General configuration ====================

    private Long timeoutMs;

    // ==================== CHAT dialogue model ====================

    private Double temperature;
    private Double topP;
    private Integer topK;
    private Integer maxTokens;
    private Double frequencyPenalty;
    private Double presencePenalty;
    private List<String> stopSequences;
    private Long seed;
    private String responseFormat;
    private Boolean stream;
    private String completionsPath;

    // ==================== EMBEDDING vector model ====================

    private Integer embeddingDimension;
    private String embeddingsPath;
    private String encodingFormat;

    // ==================== RERANKER rearrange model ====================

    private String rerankPath;
}

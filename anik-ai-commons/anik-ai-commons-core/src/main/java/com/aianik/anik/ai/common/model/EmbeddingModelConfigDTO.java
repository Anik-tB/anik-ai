package com.aianik.anik.ai.common.model;

import lombok.Data;

import java.util.List;

/**
 * (Copied from anik-job-ai-executor)
 */
@Data
public class EmbeddingModelConfigDTO {
    private List<String> inputs;
    private String modelName;
    private Integer dimensions;
}

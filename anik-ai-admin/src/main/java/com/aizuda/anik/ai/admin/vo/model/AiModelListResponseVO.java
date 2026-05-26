package com.aizuda.anik.ai.admin.vo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI model list response VO
 * Used to return a grouped/filtered model list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelListResponseVO {

    /**
     * Model type (CHAT/EMBEDDING/RERANKER/IMAGE/SPEECH)
     */
    private String modelType;

    /**
     * The default model ID for this model type
     */
    private Long defaultModelId;

    /**
     * List of models under this model type
     */
    private List<AiModelConfigVO> models;

    /**
     * Total quantity under this type
     */
    private Integer total;
}

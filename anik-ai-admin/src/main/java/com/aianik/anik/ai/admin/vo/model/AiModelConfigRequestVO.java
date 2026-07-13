package com.aianik.anik.ai.admin.vo.model;

import com.aianik.anik.ai.common.model.ConfigExtAttrsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI model configuration request VO
 * Request VO for creating or editing model configuration from the frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelConfigRequestVO {

    /**
     * Provider ID (required)
     */
    @NotNull(message = "Provider ID cannot be empty")
    private Long providerId;

    /**
     * Model name (required)
     */
    @NotBlank(message = "Model name cannot be empty")
    private String modelName;

    /**
     * Model identifier (required)
     */
    @NotBlank(message = "Model identifier cannot be empty")
    private String modelKey;

    /**
     * Model type (required)
     * CHAT, EMBEDDING, RERANKER, IMAGE, SPEECH
     */
    @NotBlank(message = "Model type cannot be empty")
    private String modelType;

    /**
     * Model description
     */
    private String description;

    /**
     * API key (required)
     */
    private String apiKey;

    /**
     * API endpoint URL
     */
    private String apiEndpoint;

    /**
     * Model parameter configuration (JSON format)
     */
    private ConfigExtAttrsDTO configJson;

    /**
     * Owner ID (optional)
     * NULL: global model
     * Specific value: user personal model
     */
    private Long ownerId;

    /**
     * scope (optional, default GLOBAL)
     * GLOBAL, PERSONAL
     */
    private String scope;

    /**
     * Whether it is the default model (optional, default false)
     */
    private Boolean isDefault;
}

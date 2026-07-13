package com.aianik.anik.ai.admin.vo.model;

import com.aianik.anik.ai.common.model.ConfigExtAttrsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI model configuration response VO
 * Used to return to the front end to hide sensitive information (API Key desensitization)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelConfigVO {

    private Long id;
    private Long providerId;
    private String providerName;
    private String modelName;
    private String modelKey;
    private String modelType;
    private String description;
    private String apiKey;  // Masked API Key
    private String apiEndpoint;
    private ConfigExtAttrsDTO configJson;
    private Long ownerId;
    private String scope;
    private Boolean isDefault;
    private Boolean isEnabled;
    private LocalDateTime createdDt;
    private LocalDateTime updatedDt;
}

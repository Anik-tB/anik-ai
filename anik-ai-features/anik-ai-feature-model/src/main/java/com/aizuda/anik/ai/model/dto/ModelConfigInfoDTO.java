package com.aizuda.anik.ai.model.dto;

import com.aizuda.anik.ai.common.model.ConfigExtAttrsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2026-03-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfigInfoDTO {
    /**
     * Configuration ID
     */
    private Long id;

    /**
     * Model name
     */
    private String modelName;

    /**
     * model identifier
     */
    private String modelKey;

    /**
     * Provider ID
     */
    private Long providerId;

    /**
     * Provider name
     */
    private String providerName;

    /**
     * Provider ID
     */
    private String providerKey;

    /**
     * Model type (CHAT/EMBEDDING/RERANKER/IMAGE/SPEECH)
     */
    private String modelType;

    /**
     * describe
     */
    private String description;

    /**
     * Scope (GLOBAL=overall situation, PERSONAL=personal)
     */
    private String scope;

    /**
     * Is it the default model?
     */
    private Boolean isDefault;

    /**
     * Whether enabled
     */
    private Boolean enabled;

    /**
     * API endpoint (optional, some providers may require custom endpoints)
     */
    private String apiEndpoint;

    /**
     * Model configurationJSON (for model specific parameters, such as temperature, etc.)
     * Only used internally, not returned to the outside world
     */
    private ConfigExtAttrsDTO configJson;

    /**
     * Encrypted API Key (for internal transmission, not exposed to the outside world)
     */
    private String encryptedApiKey;
}

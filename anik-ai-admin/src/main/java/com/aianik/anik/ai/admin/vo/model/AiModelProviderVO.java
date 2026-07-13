package com.aianik.anik.ai.admin.vo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI model provider VO
 * Used to return provider information to the front end
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelProviderVO {

    /**
     * Provider ID
     */
    private Long id;

    /**
     * Provider name
     */
    private String providerName;

    /**
     * provider identifier
     */
    private String providerKey;

    /**
     * Provider description
     */
    private String description;

    /**
     * LOGO icon URL
     */
    private String iconUrl;

    /**
     * Whether enabled
     */
    private Boolean isEnabled;

    /**
     * Creation timestamp (milliseconds)
     */
    private LocalDateTime createdDt;

    /**
     * Update timestamp (milliseconds)
     */
    private LocalDateTime updatedDt;
}

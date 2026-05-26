package com.aizuda.anik.ai.admin.vo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Model Usage Statistics VO
 * Used to return model usage statistics to the front end
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelUsageStatVO {

    /**
     * Statistics record ID
     */
    private Long id;

    /**
     * Model ID
     */
    private Long modelId;

    /**
     * Model name
     */
    private String modelName;

    /**
     * Model type
     */
    private String modelType;

    /**
     * Provider ID
     */
    private Long providerId;

    /**
     * Provider name
     */
    private String providerName;

    /**
     * userID
     */
    private Long userId;

    /**
     * total calls
     */
    private Long totalCalls;

    /**
     * Number of successful calls
     */
    private Long successCalls;

    /**
     * Number of failed calls
     */
    private Long failedCalls;

    /**
     * Success rate (0-100)
     */
    private Double successRate;

    /**
     * Total Token usage
     */
    private Long totalTokensUsed;

    /**
     * total cost
     */
    private BigDecimal totalCost;

    /**
     * Average response time (milliseconds)
     */
    private Long avgResponseTime;

    /**
     * Last used time (ISO8601 format or timestamp)
     */
    private Long lastUsedDt;

    /**
     * Creation timestamp (milliseconds)
     */
    private Long createdDt;

    /**
     * Update timestamp (milliseconds)
     */
    private Long updatedDt;
}

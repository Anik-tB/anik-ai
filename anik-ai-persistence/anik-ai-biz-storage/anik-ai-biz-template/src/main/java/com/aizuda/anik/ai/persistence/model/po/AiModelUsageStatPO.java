package com.aizuda.anik.ai.persistence.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model uses statistical persistence objects
 * Table: anik_ai_model_usage_stat
 *
 * Statistics usage by model and user dimensions
 * Periodically aggregate refresh from the usage_log table
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("anik_ai_model_usage_stat")
public class AiModelUsageStatPO {

    /**
     * Statistics record ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Model ID (foreign key)
     * Related to anik_ai_model_config.id
     */
    private Long modelId;

    /**
     * userID (foreign key)
     * Linked to anik_ai_user.id
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
     * Total Token usage
     */
    private Long totalTokensUsed;

    /**
     * Total cost (optional)
     * If there is pricing information in the configuration, calculate this value
     */
    private BigDecimal totalCost;

    /**
     * Average response time (milliseconds)
     */
    private Long avgResponseTime;

    /**
     * last use time
     */
    private LocalDateTime lastUsedDt;

    /**
     * creation time
     */
    private LocalDateTime createdDt;

    /**
     * Update time
     */
    private LocalDateTime updatedDt;
}

package com.aianik.anik.ai.persistence.agent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Agent uses statistical persistence objects
 * Table: anik_ai_agent_usage_stat
 *
 * Usage statistics by Agent and user dimensions
 * Aggregate statistics on messages, sessions and other indicators by day
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_agent_usage_stat")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentUsageStatPO {

    /**
     * Statistics ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent ID (foreign key)
     * Linked to anik_ai_agent.id
     */
    private Long agentId;

    /**
     * userID (foreign key)
     * Linked to anik_ai_user.id
     */
    private Long userId;

    /**
     * user name
     * Snapshot storage for easy report display
     */
    private String userName;

    /**
     * department
     * The department to which the user belongs (optional)
     */
    private String department;

    /**
     * Number of messages
     * The total number of messages for this Agent and this user on that day
     */
    private Integer messageCount;

    /**
     * Number of sessions
     * The number of newly created sessions for this Agent and this user on this day
     */
    private Integer conversationCount;

    /**
     * Statistics date
     * The date for which statistics are collected
     */
    private LocalDate statDate;

    /**
     * creation time
     * The moment the statistics record was created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * Statistics record the last moment of renewal
     */
    private LocalDateTime updateDt;
}

package com.aizuda.anik.ai.persistence.agent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dialog Session Persistence Object
 * Table: anik_ai_agent_conversation
 *
 * Represents a complete conversation session between Agent and user
 * A session contains multiple conversation records (AgentConversationRecordPO)
 * Supports session management, title, creation time, etc.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_agent_conversation")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentConversationPO {

    /**
     * Session ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * agentID (foreign key)
     * Linked to anik_ai_agent.id
     * Agent to which this session belongs
     */
    private Long agentId;

    /**
     * userID (foreign key)
     * Linked to anik_ai_user.id
     * The user of this session
     */
    private Long userId;

    /**
     * session unique identifier
     * Session ID used by the business layer (UUID format)
     * Different from auto-increment id, used for external references
     */
    private String conversationId;

    /**
     * Session title
     * User-defined or system-generated session name
     * For example: "How to learn Java", "Product requirements discussion"
     */
    private String title;

    /**
     * creation time
     * The moment when the session is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The moment when the session was last renewed (usually the time of the last message)
     */
    private LocalDateTime updateDt;
}

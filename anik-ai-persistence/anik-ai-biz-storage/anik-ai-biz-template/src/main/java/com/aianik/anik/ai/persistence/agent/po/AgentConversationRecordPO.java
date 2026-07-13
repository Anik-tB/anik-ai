package com.aianik.anik.ai.persistence.agent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Conversation record persistence object
 * Table: anik_ai_agent_conversation_record
 *
 * Record every conversation message between user and Agent
 * Supports multiple rounds of conversation tracking, context recovery, memory retrieval, etc.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_agent_conversation_record")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentConversationRecordPO {

    /**
     * Record ID (primary key)
     * Auto-increment primary key, unique in overall situation
     * Also used for memory checkpoint tracking
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * agentID (foreign key)
     * Linked to anik_ai_agent.id
     * Identify which Agent the message comes from
     */
    private Long agentId;

    /**
     * Session ID (foreign key)
     * Linked to anik_ai_agent_conversation.id
     * Messages within the same conversation share the same conversationId for multi-round conversation association.
     */
    private String conversationId;

    /**
     * userID (foreign key)
     * Linked to anik_ai_user.id
     * The user who sent the message
     */
    private Long userId;

    /**
     * message role
     * USER: User messages
     * ASSISTANT: Assistant/Agent reply
     * SYSTEM: system message
     */
    private String role;

    /**
     * Message content
     * Complete message text content
     * May contain markdown format, code blocks, etc.
     */
    private String content;

    /**
     * thought process
     * The model’s thinking chain/reasoning process (assistant role only)
     */
    private String thinking;

    /**
     * MessageStatus
     * 0: Draft/Not Sent
     * 1: Sent
     * 2: Processed
     * -1: Processing failed
     */
    private Integer status;

    /**
     * Token quantity statistics
     * The number of tokens consumed for this message
     * Used for statistics and cost calculations
     */
    private Integer tokenCount;

    /**
     * creation time
     * The creation time of the message record
     */
    private LocalDateTime createDt;
}

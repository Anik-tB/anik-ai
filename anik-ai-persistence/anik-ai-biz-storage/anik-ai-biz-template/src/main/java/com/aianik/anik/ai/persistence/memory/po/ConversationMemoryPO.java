package com.aianik.anik.ai.persistence.memory.po;

import com.aianik.anik.ai.common.enums.memory.MemoryStatusEnum;
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
 * Conversation memory persistence object
 * Table: anik_ai_memory_conversation
 *
 * Store structured memory extracted from LLM in multiple rounds of dialogue
 * Including fact, decision making, Preference, Task progress, etc.
 * Support vectorized storage and semantic retrieval
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_memory_conversation")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationMemoryPO {

    /**
     * Memory ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * agentID (foreign key)
     * Linked to anik_ai_agent.id
     * The Agent to which this memory belongs
     */
    private Long agentId;

    /**
     * userID (foreign key)
     * Linked to anik_ai_user.id
     * The user involved in this memory
     */
    private Long userId;

    /**
     * Creator ID (foreign key, can be null)
     * Linked to anik_ai_user.id
     * The operator who created or renewed the memory
     */
    private Long actorId;

    /**
     * Creator role
     * USER: user create/renew
     * AGENT: Agent creation/renew
     * SYSTEM: system automatically creates/renew
     */
    private Integer actorRole;

    /**
     * Session ID (foreign key)
     * Linked to anik_ai_agent_conversation.id
     * The session from which this memory originated
     */
    private String conversationId;

    /**
     * Source message ID (foreign key, can be null)
     * Related to anik_ai_agent_conversation_record.id
     * The original message from which this memory was extracted
     */
    private Long sourceMessageId;

    /**
     * memory type
     * FACT: fact information (user identity, background, etc.)
     * DECISION: decision making/choice
     * PREFERENCE: Preference/preference
     * TASK_PROGRESS: Task progress
     * REFERENCE: reference information
     */
    private Integer memoryType;

    /**
     * memory classification
     * Secondary classification for more fine-grained grouping
     * For example: PERSONAL, WORK, LEARNING, etc.
     */
    private String category;

    /**
     * memory title
     * A short identifier for memory, easy to display and understand
     */
    private String title;

    /**
     * memory content
     * Structured mnemonic text containing specific information
     */
    private String content;

    /**
     * Content Hash (SHA-256)
     * For fast deduplication and integrity verification
     * Different memory instances of the same content can be identified through this field
     */
    private String memoryHash;

    /**
     * Remember tags (JSON array format)
     * For example: ["important", "client", "2026"]
     * Supports multi-label tagging and classification
     */
    private String tags;

    /**
     * Vector store instance ID (foreign key)
     * Linked to anik_ai_store_instance.id
     * The vector storage location of this memory
     */
    private Long vectorStoreInstanceId;

    /**
     * Vector ID (foreign key)
     * The unique identifier of the vector corresponding to this memory in the vector library
     * For vector retrieval and renew
     */
    private String vectorId;

    /**
     * Relevance score
     * Value range: 0.0-1.0
     * Indicates how relevant the memory is to the current query/context
     */
    private BigDecimal relevanceScore;

    /**
     * Confidence score
     * Value range: 0.0-1.0
     * Indicates the accuracy and credibility of the memory
     * Evaluated by LLM at extraction time
     */
    private BigDecimal confidenceScore;

    /**
     * memory state
     * ACTIVE: valid/active
     * ARCHIVED: Archive/History
     * DELETED: delete/Disable
     * Use MemoryStatusEnum enumeration management
     */
    private MemoryStatusEnum status;

    /**
     * last access time
     * Record the last time this memory was retrieved/used
     * Used for access heat calculation
     */
    private LocalDateTime accessedAt;

    /**
     * access count
     * The cumulative number of times this memory has been retrieved/used
     * Used for popularity ranking and importance evaluation
     */
    private Integer accessCount;

    /**
     * creation time
     * The moment a memory is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * Remember the last modified time
     */
    private LocalDateTime updateDt;

    /**
     * Expiration time
     * null: memory never expires
     * Specific value: Memory expiration time. Memories beyond this time will be cleared.
     * Calculated by MemoryPO.memoryExpirationDays on write
     */
    private LocalDateTime expiresAt;
}

package com.aizuda.anik.ai.persistence.memory.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Memory change history persistent object
 * Table: anik_ai_memory_conversation_history
 *
 * Complete change audit log recording every memory
 * Supports version backtracking, audit tracking, change analysis, etc.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_memory_conversation_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemoryHistoryPO {

    /**
     * History ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Memory ID (foreign key)
     * Related to anik_ai_conversation_memory.id
     * The memory to which this history belongs
     */
    private Long memoryId;

    /**
     * Vector ID (foreign key)
     * The unique identifier of the corresponding memory in the vector library
     * Used to track operations in the vector library
     */
    private String vectorId;

    /**
     * Change event type
     * ADD: New memory
     * UPDATE: renew memory
     * DELETE: delete memory
     * NOOP: No action (evaluated but not changed)
     */
    private Integer event;

    /**
     * Memory content before change
     * A snapshot of the memory before the change (usually JSON)
     * DELETE event is the complete memory before delete
     * ADD event is null
     */
    private String oldMemory;

    /**
     * Changed memory content
     * A snapshot of the memory after the change (usually JSON)
     * ADD/UPDATE event is the new/updated memory
     * DELETE event is null
     */
    private String newMemory;

    /**
     * Operator userID (foreign key, can be null)
     * Linked to anik_ai_user.id
     * The user who performed the change (null if it was an automatic operation)
     */
    private Long actorId;

    /**
     * Operator role
     * USER: user manual operation
     * AGENT: Agent automatic operation
     * SYSTEM: system automatic operation
     */
    private Integer actorRole;

    /**
     * creation time
     * The execution time of the change operation
     */
    private LocalDateTime createDt;
}

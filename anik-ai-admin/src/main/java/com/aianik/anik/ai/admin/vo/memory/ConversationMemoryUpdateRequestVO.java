package com.aianik.anik.ai.admin.vo.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dialogue Memory DTO
 *
 * @author anik-ai
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationMemoryUpdateRequestVO {

    /**
     * Primary key ID
     */
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * userID
     */
    private Long userId;

    /**
     * Conversation ID
     */
    private String conversationId;

    /**
     * Source message ID
     */
    private Long sourceMessageId;

    /**
     * memory type
     */
    private Integer memoryType;

    /**
     * category
     */
    private String category;

    /**
     * title
     */
    private String title;

    /**
     * content
     */
    private String content;

    /**
     * Label
     */
    private List<String> tags;

    /**
     * Vector store instance ID
     */
    private Long vectorStoreInstanceId;

    /**
     * Vector ID
     */
    private String vectorId;

    /**
     * Vector embedding (used for inserting into vector store)
     */
    private List<Float> embedding;

    /**
     * Relevance score
     */
    private BigDecimal relevanceScore;

    /**
     * Confidence score
     */
    private BigDecimal confidenceScore;

    /**
     * Status
     */
    private Integer status;

    /**
     * Visits
     */
    private Integer accessCount;

    /**
     * last access time
     */
    private LocalDateTime accessedAt;

    /**
     * creation time
     */
    private LocalDateTime createDt;

    /**
     * Update time
     */
    private LocalDateTime updateDt;
}

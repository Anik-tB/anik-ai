package com.aizuda.anik.ai.admin.vo.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Memory search request VO
 *
 * @author anik-ai
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemorySearchRequestVO {

    /**
     * search query
     */
    private String query;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * Conversation ID (retrieval with session weighting, etc., optional)
     */
    private String conversationId;

    /**
     * Embed model ID (semantic retrieval, optional, handled by the server side by default)
     */
    private Long embeddingModelId;

    /**
     * memory type filter
     */
    private List<Integer> types;

    /**
     * limited quantity
     */
    private Integer limit;

    /**
     * recent days
     */
    private Integer days;
}

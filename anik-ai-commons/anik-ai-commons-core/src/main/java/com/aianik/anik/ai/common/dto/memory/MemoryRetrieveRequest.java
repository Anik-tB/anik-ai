package com.aianik.anik.ai.common.dto.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Retrieve memory context request
 *
 * @author openanik
 * @date 2025-04-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryRetrieveRequest {
    
    /**
     * Agent ID
     */
    private Long agentId;
    
    /**
     * user ID
     */
    private Long userId;
    
    /**
     * Conversation ID
     */
    private String conversationId;
    
    /**
     * question
     */
    private String query;
    
    /**
     * Memory configuration ID
     */
    private Long memoryConfigId;
    
    /**
     * Model ID
     */
    private Long modelId;
    
    /**
     * Top K
     */
    private Integer topK;
}

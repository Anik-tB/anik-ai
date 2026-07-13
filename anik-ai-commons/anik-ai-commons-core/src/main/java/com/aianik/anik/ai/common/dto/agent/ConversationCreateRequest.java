package com.aianik.anik.ai.common.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create conversation request
 *
 * @author openanik
 * @date 2025-04-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationCreateRequest {

    private Long agentId;
    private Long userId;
    private String conversationId;
    private String userMessage;
}

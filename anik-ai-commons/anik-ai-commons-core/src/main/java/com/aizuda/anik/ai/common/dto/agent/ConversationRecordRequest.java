package com.aizuda.anik.ai.common.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Save conversation history request
 *
 * @author openanik
 * @date 2025-04-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRecordRequest {

    private Long agentId;
    private Long userId;
    private String conversationId;
    private String role;
    private String content;
    private String thinking;
}

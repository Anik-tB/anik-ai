package com.aianik.anik.ai.common.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAPI synchronous conversation response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenApiChatSyncResponse {

    private String conversationId;

    private String content;

    private String traceId;

    private Long durationMs;
}

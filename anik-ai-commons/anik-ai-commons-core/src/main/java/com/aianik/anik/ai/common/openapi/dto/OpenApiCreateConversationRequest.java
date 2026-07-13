package com.aianik.anik.ai.common.openapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OpenAPI Create session request
 */
@Data
public class OpenApiCreateConversationRequest {

    @NotNull(message = "agentId is required")
    private Long agentId;

    @NotBlank(message = "openId is required")
    private String openId;

    @NotBlank(message = "title is required")
    private String title;
}

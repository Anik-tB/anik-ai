package com.aizuda.anik.ai.common.openapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * OpenAPI Conversation request
 */
@Data
public class OpenApiChatRequest {

    @NotNull(message = "agentId is required")
    private Long agentId;

    @NotBlank(message = "openId is required")
    private String openId;

    @NotBlank(message = "conversationId is required")
    private String conversationId;

    @NotBlank(message = "content is required")
    private String content;

    private List<Long> disabledMcpServerIds;

    private List<Long> disabledSkillIds;
}

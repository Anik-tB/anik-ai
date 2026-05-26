package com.aizuda.anik.ai.common.openapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OpenAPI conversation query request (query parameter)
 */
@Data
public class OpenApiConversationQueryRequest {

    @NotNull(message = "agentId is required")
    private Long agentId;

    @NotBlank(message = "openId is required")
    private String openId;

    @Min(value = 1, message = "page must be greater than 0")
    private Integer page = 1;

    @Min(value = 1, message = "size must be greater than 0")
    private Integer size = 20;
}

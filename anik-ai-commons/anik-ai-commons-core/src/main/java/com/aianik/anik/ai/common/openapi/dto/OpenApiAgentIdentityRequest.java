package com.aianik.anik.ai.common.openapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * OpenAPI Agent Identity Request
 */
@Data
public class OpenApiAgentIdentityRequest {

    @NotNull(message = "agentId is required")
    private Long agentId;
}

package com.aizuda.anik.ai.common.openapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OpenAPI user query request
 */
@Data
public class OpenApiUserQueryRequest {

    @NotBlank(message = "openId is required")
    private String openId;
}

package com.aizuda.anik.ai.common.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAPI message logging
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenApiMessageVO {

    private String role;

    private String content;

    private Integer status;

    private String createDt;
}

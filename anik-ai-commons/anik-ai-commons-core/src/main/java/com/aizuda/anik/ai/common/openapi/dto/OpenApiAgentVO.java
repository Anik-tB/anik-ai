package com.aizuda.anik.ai.common.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAPI Agent Summary Information
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenApiAgentVO {

    private Long id;

    private String name;

    private String description;

    private String avatar;

    private String greeting;

    private Integer status;
}

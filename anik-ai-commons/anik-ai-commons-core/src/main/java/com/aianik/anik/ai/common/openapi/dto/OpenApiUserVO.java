package com.aianik.anik.ai.common.openapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OpenAPI user info response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpenApiUserVO {

    private String openId;

    private String externalId;

    private String nickname;

    private boolean created;
}

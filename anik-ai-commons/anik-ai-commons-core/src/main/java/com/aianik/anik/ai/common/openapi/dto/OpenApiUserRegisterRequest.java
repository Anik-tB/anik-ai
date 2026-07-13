package com.aianik.anik.ai.common.openapi.dto;

import lombok.Data;

/**
 * OpenAPI User registration request
 */
@Data
public class OpenApiUserRegisterRequest {

    private String externalId;

    private String nickname;
}

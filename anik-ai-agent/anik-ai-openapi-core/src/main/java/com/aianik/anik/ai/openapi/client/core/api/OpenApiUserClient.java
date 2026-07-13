package com.aianik.anik.ai.openapi.client.core.api;

import com.aianik.anik.ai.common.constants.OpenApiPathConstants;
import com.aianik.anik.ai.common.model.Result;
import com.aianik.anik.ai.common.openapi.dto.OpenApiUserRegisterRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiUserQueryRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiUserVO;
import com.aianik.anik.ai.openapi.client.core.annotation.OpenApiMapping;

public interface OpenApiUserClient {

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_USER_REGISTER, method = OpenApiMapping.HttpMethod.POST)
    Result<OpenApiUserVO> register(OpenApiUserRegisterRequest request);

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_USER, method = OpenApiMapping.HttpMethod.GET)
    Result<OpenApiUserVO> getUser(OpenApiUserQueryRequest request);
}

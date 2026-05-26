package com.aizuda.anik.ai.openapi.controller;

import com.aizuda.anik.ai.common.constants.OpenApiPathConstants;
import com.aizuda.anik.ai.common.model.Result;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiUserQueryRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiUserRegisterRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiUserVO;
import com.aizuda.anik.ai.openapi.service.OpenApiUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OpenApiUserController {

    private final OpenApiUserService openApiUserService;

    @PostMapping(OpenApiPathConstants.OPEN_API_USER_REGISTER)
    public Result<OpenApiUserVO> register(@RequestBody OpenApiUserRegisterRequest request) {
        return Result.ok(openApiUserService.register(request));
    }

    @GetMapping(OpenApiPathConstants.OPEN_API_USER)
    public Result<OpenApiUserVO> getUser(@Validated OpenApiUserQueryRequest request) {
        return Result.ok(openApiUserService.getByOpenId(request.getOpenId()));
    }
}

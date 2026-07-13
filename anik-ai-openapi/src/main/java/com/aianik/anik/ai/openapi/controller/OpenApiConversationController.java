package com.aianik.anik.ai.openapi.controller;

import com.aianik.anik.ai.admin.vo.PageResult;
import com.aianik.anik.ai.common.constants.OpenApiPathConstants;
import com.aianik.anik.ai.common.model.Result;
import com.aianik.anik.ai.common.openapi.dto.OpenApiConversationIdentityRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiConversationQueryRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiCreateConversationRequest;
import com.aianik.anik.ai.openapi.dto.OpenApiConversationVO;
import com.aianik.anik.ai.openapi.dto.OpenApiMessageVO;
import com.aianik.anik.ai.openapi.service.OpenApiConversationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OpenAPI session management interface
 *
 * @author openanik
 * @date 2026-04-24
 */
@RestController
@RequiredArgsConstructor
@Validated
public class OpenApiConversationController {

    private final OpenApiConversationService openApiConversationService;

    @GetMapping(OpenApiPathConstants.OPEN_API_AGENT_CONVERSATIONS)
    public PageResult<List<OpenApiConversationVO>> listConversations(
            @Validated OpenApiConversationQueryRequest request) {
        return openApiConversationService.listConversations(request);
    }

    @PostMapping(OpenApiPathConstants.OPEN_API_AGENT_CONVERSATIONS)
    public Result<OpenApiConversationVO> createConversation(
            @RequestBody @Validated OpenApiCreateConversationRequest request) {
        return Result.ok(openApiConversationService.createConversation(request));
    }

    @DeleteMapping(OpenApiPathConstants.OPEN_API_AGENT_CONVERSATIONS)
    public Result<Void> deleteConversation(
            @Validated OpenApiConversationIdentityRequest request) {
        openApiConversationService.deleteConversation(request);
        return Result.ok(null);
    }

    @GetMapping(OpenApiPathConstants.OPEN_API_AGENT_CONVERSATION_MESSAGES)
    public Result<List<OpenApiMessageVO>> getMessages(
            @Validated OpenApiConversationIdentityRequest request) {
        return Result.ok(openApiConversationService.getMessages(request));
    }
}

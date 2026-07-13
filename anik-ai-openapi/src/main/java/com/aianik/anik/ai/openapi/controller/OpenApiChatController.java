package com.aianik.anik.ai.openapi.controller;

import com.aianik.anik.ai.common.constants.OpenApiPathConstants;
import com.aianik.anik.ai.common.model.Result;
import com.aianik.anik.ai.common.openapi.dto.OpenApiChatRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiChatSyncResponse;
import com.aianik.anik.ai.openapi.service.OpenApiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * OpenAPI conversational interface
 *
 * @author openanik
 * @date 2026-04-24
 */
@RestController
@RequiredArgsConstructor
public class OpenApiChatController {

    private final OpenApiChatService openApiChatService;

    @PostMapping(value = OpenApiPathConstants.OPEN_API_AGENT_CHAT, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody @Validated OpenApiChatRequest request) {
        return openApiChatService.chatStream(request);
    }

    @PostMapping(OpenApiPathConstants.OPEN_API_AGENT_CHAT_SYNC)
    public Result<OpenApiChatSyncResponse> chatSync(@RequestBody @Validated OpenApiChatRequest request) {
        return Result.ok(openApiChatService.chatSync(request));
    }
}

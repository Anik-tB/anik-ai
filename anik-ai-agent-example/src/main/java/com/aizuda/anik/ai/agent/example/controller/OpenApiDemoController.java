package com.aizuda.anik.ai.agent.example.controller;

import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.common.model.PageResult;
import com.aizuda.anik.ai.common.model.Result;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiAgentVO;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiChatRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiChatSyncResponse;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiConversationIdentityRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiConversationQueryRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiConversationVO;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiCreateConversationRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiMessageVO;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiAgentIdentityRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiUserRegisterRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiUserQueryRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiUserVO;
import com.aizuda.anik.ai.openapi.client.core.api.OpenApiAgentClient;
import com.aizuda.anik.ai.openapi.client.core.api.OpenApiChatClient;
import com.aizuda.anik.ai.openapi.client.core.api.OpenApiConversationClient;
import com.aizuda.anik.ai.openapi.client.core.api.OpenApiUserClient;
import com.aizuda.anik.ai.openapi.client.core.listener.SseEventListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * OpenAPI usage example Controller
 * <p>
 * Demonstrate how to use OpenAPI Client to call Anik AI server interface
 * 
 * <pre>
 * Visit Swagger UI: http://localhost:17889/swagger-ui.html
 * </pre>
 *
 * @author openanik
 * @date 2026-04-25
 */
@Slf4j
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
@Tag(name = "OpenAPI Demo", description = "OpenAPI client usage example")
public class OpenApiDemoController {

    private final OpenApiAgentClient agentClient;
    private final OpenApiChatClient chatClient;
    private final OpenApiConversationClient conversationClient;
    private final OpenApiUserClient userClient;

    // ==================== User related interfaces ====================

    @PostMapping("/user/register")
    @Operation(summary = "Registered user", description = "Register or update user information, return openId")
    public Result<OpenApiUserVO> registerUser(
            @Parameter(description = "User registration request")
            @RequestBody OpenApiUserRegisterRequest request) {
        log.info("Register user: externalId={}, nickname={}", request.getExternalId(), request.getNickname());
        return userClient.register(request);
    }

    @GetMapping("/user")
    @Operation(summary = "Get user information", description = "Query user details based on openId")
    public Result<OpenApiUserVO> getUser(
            @Parameter(description = "openId", required = true, example = "demo-open-id")
            @RequestParam String openId) {
        OpenApiUserQueryRequest request = new OpenApiUserQueryRequest();
        request.setOpenId(openId);
        return userClient.getUser(request);
    }

    //==================== Agent related interfaces ====================

    @GetMapping("/agents")
    @Operation(summary = "Get a list of all Agents", description = "Query all agents accessible to the current user")
    public Result<List<OpenApiAgentVO>> listAgents() {
        return agentClient.listAgents();
    }

    @GetMapping("/agent/{agentId}")
    @Operation(summary = "Get Agent details", description = "Query agent details based on ID")
    public Result<OpenApiAgentVO> getAgent(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId) {
        OpenApiAgentIdentityRequest request = new OpenApiAgentIdentityRequest();
        request.setAgentId(agentId);
        return agentClient.getAgent(request);
    }

    // ==================== Conversation related interfaces ====================

    @PostMapping("/agent/{agentId}/conversation")
    @Operation(summary = "Create session", description = "Creates a new conversation session for the specified Agent")
    public Result<OpenApiConversationVO> createConversation(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId,
            @Parameter(description = "Create session request") 
            @RequestBody OpenApiCreateConversationRequest request) {
        request.setAgentId(agentId);
        return conversationClient.createConversation(request);
    }

    @GetMapping("/agent/{agentId}/conversations")
    @Operation(summary = "Get session list", description = "Query all sessions of the specified Agent (pagination)")
    public PageResult<List<OpenApiConversationVO>> listConversations(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId,
            @Parameter(description = "openId", required = true, example = "demo-open-id")
            @RequestParam String openId,
            @Parameter(description = "page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Quantity per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        OpenApiConversationQueryRequest request = new OpenApiConversationQueryRequest();
        request.setAgentId(agentId);
        request.setOpenId(openId);
        request.setPage(page);
        request.setSize(size);
        return conversationClient.listConversations(request);
    }

    @GetMapping("/agent/{agentId}/conversation/{conversationId}/messages")
    @Operation(summary = "Get session messages", description = "Query all message records of a specified session")
    public Result<List<OpenApiMessageVO>> getMessages(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId,
            @Parameter(description = "Session ID", required = true, example = "conv-123")
            @PathVariable String conversationId,
            @Parameter(description = "openId", required = true, example = "demo-open-id")
            @RequestParam String openId) {
        OpenApiConversationIdentityRequest request = new OpenApiConversationIdentityRequest();
        request.setAgentId(agentId);
        request.setConversationId(conversationId);
        request.setOpenId(openId);
        return conversationClient.getMessages(request);
    }

    @DeleteMapping("/agent/{agentId}/conversation/{conversationId}")
    @Operation(summary = "Delete session", description = "Delete the specified conversation session")
    public Result<Void> deleteConversation(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId,
            @Parameter(description = "Session ID", required = true, example = "conv-123")
            @PathVariable String conversationId,
            @Parameter(description = "openId", required = true, example = "demo-open-id")
            @RequestParam String openId) {
        OpenApiConversationIdentityRequest request = new OpenApiConversationIdentityRequest();
        request.setAgentId(agentId);
        request.setConversationId(conversationId);
        request.setOpenId(openId);
        return conversationClient.deleteConversation(request);
    }

    // ==================== Chat related interfaces ====================

    @PostMapping("/agent/{agentId}/chat/sync")
    @Operation(summary = "synchronous conversation", description = "Send message and wait for AI reply (non-streaming)")
    public Result<OpenApiChatSyncResponse> chatSync(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId,
            @Parameter(description = "Conversation request")
            @RequestBody OpenApiChatRequest request) {
        request.setAgentId(agentId);
        log.info("Sync chat request: agentId={}, content={}", agentId, request.getContent());
        return chatClient.chatSync(request);
    }

    @GetMapping("/agent/{agentId}/chat/stream")
    @Operation(summary = "streaming conversation", description = "Send messages and receive AI replies with SSE streaming")
    public SseEmitter chatStream(
            @Parameter(description = "Agent ID", required = true, example = "1")
            @PathVariable Long agentId,
            @Parameter(description = "openId", required = true, example = "demo-open-id")
            @RequestParam String openId,
            @Parameter(description = "User messages", required = true, example = "Hello")
            @RequestParam String content,
            @Parameter(description = "Session ID (optional)", example = "conv-123")
            @RequestParam(required = false) String conversationId) {

        SseEmitter emitter = new SseEmitter(300000L); //5 minute timeout

        OpenApiChatRequest request = new OpenApiChatRequest();
        request.setAgentId(agentId);
        request.setOpenId(openId);
        request.setContent(content);
        request.setConversationId(conversationId);

        log.info("Stream chat request: agentId={}, content={}", agentId, content);

        //Execute streaming conversation asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                chatClient.chatStream(request, new SseEventListener() {
                    @Override
                    public void onText(String text) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("text")
                                    .data(text));
                        } catch (IOException e) {
                            log.error("Failed to send SSE text", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onThinking(String thinking) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("thinking")
                                    .data(thinking));
                        } catch (IOException e) {
                            log.error("Failed to send SSE thinking", e);
                        }
                    }

                    @Override
                    public void onComplete(String data) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data(data));
                            emitter.complete();
                            log.info("Stream chat completed");
                        } catch (IOException e) {
                            log.error("Failed to send SSE completion", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        log.error("Stream chat error: {}", errorMessage);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(errorMessage));
                        } catch (IOException e) {
                            log.error("Failed to send SSE error", e);
                        }
                        emitter.completeWithError(new AnikAiException(errorMessage));
                    }
                });
            } catch (Exception e) {
                log.error("Stream chat exception", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}

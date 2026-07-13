package com.aianik.anik.ai.openapi.service;

import com.aianik.anik.ai.admin.dto.AgentChatCommand;
import com.aianik.anik.ai.admin.service.agent.AgentChatService;
import com.aianik.anik.ai.common.execption.AnikAiException;
import com.aianik.anik.ai.common.openapi.dto.OpenApiChatRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiChatSyncResponse;
import com.aianik.anik.ai.openapi.emitter.CollectingEmitter;
import com.aianik.anik.ai.openapi.emitter.SseWrappingEmitter;
import com.aianik.anik.ai.openapi.security.OpenApiSessionUtils;
import com.aianik.anik.ai.persistence.admin.po.UserPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.TimeoutException;

/**
 * OpenAPI conversation service (simplified)
 * AgentChatService has been removed, this service retains the interface signature to ensure compatibility
 *
 * @author openanik
 * @date 2026-04-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiChatService {

    private static final long SYNC_CHAT_TIMEOUT_MS = 300_000L;
    private final OpenApiUserResolver openApiUserResolver;
    private final AgentChatService agentChatService;

    /**
     * streaming conversation — Returns an SseEmitter that pushes each text chunk via SSE events
     */
    public SseEmitter chatStream(OpenApiChatRequest request) {
        SseEmitter sseEmitter = new SseEmitter(0L);
        SseWrappingEmitter wrapper = new SseWrappingEmitter(sseEmitter, request.getConversationId());
        UserPO requestUser = resolveRequestUser(request.getOpenId());

        agentChatService.chat(AgentChatCommand.builder()
                .agentId(request.getAgentId())
                .conversationId(request.getConversationId())
                .content(request.getContent())
                .disabledMcpServerIds(request.getDisabledMcpServerIds())
                .disabledSkillIds(request.getDisabledSkillIds())
                .emitter(wrapper)
                .requestUser(requestUser)
                .openId(request.getOpenId())
                .build());

        return sseEmitter;
    }

    /**
     * synchronous conversation — blocks waiting for a complete response
     */
    public OpenApiChatSyncResponse chatSync(OpenApiChatRequest request) {
        long start = System.currentTimeMillis();
        CollectingEmitter collector = new CollectingEmitter();
        UserPO requestUser = resolveRequestUser(request.getOpenId());

        agentChatService.chat(AgentChatCommand.builder()
                .agentId(request.getAgentId())
                .conversationId(request.getConversationId())
                .content(request.getContent())
                .disabledMcpServerIds(request.getDisabledMcpServerIds())
                .disabledSkillIds(request.getDisabledSkillIds())
                .emitter(collector)
                .requestUser(requestUser)
                .openId(request.getOpenId())
                .build());

        try {
            String fullText = collector.awaitAndGetFullText(SYNC_CHAT_TIMEOUT_MS);
            return OpenApiChatSyncResponse.builder()
                    .conversationId(request.getConversationId())
                    .content(fullText)
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnikAiException("Conversation interrupted", e);
        } catch (TimeoutException e) {
            throw new AnikAiException("Conversation response timeout", e);
        }
    }

    private UserPO resolveRequestUser(String openId) {
        String appId = OpenApiSessionUtils.current().getAppId();
        return openApiUserResolver.resolvePlatformUser(appId, openId);
    }
}

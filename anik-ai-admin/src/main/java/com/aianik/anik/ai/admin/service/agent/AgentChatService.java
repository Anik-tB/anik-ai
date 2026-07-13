package com.aianik.anik.ai.admin.service.agent;

import com.aianik.anik.ai.admin.dto.AgentChatCommand;
import com.aianik.anik.ai.feature.agent.chain.AgentChatContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentChatService {
    private final AgentChatChainService agentChatChainService;

    @SuppressWarnings("unused")
    private static final String DEFAULT_INSTRUCTION = "You are an intelligent assistant.";

    /**
     * agentstreaming conversation——completed sequentially through the chain of responsibility: initialization context → session management → model parsing →
     * MCP loading → system prompt word construction → Skill injection → RAG retrieval → LLM call
     */
    public void chat(AgentChatCommand command) {
        //Generate traceId and root SPAN ID
        String traceId = UUID.randomUUID().toString();
        String rootSpanId = UUID.randomUUID().toString();
        long rootSpanStart = System.currentTimeMillis();

        AgentChatContext context = new AgentChatContext(
                command.getAgentId(),
                command.getConversationId(),
                command.getContent(),
                command.getEmitter(),
                command.getRequestUser(),
                command.getOpenId());
        context.setDisabledMcpServerIds(command.getDisabledMcpServerIds());
        context.setDisabledSkillIds(command.getDisabledSkillIds());
        context.setTraceId(traceId);
        context.setRootSpanId(rootSpanId);
        context.setRootSpanStartTimeMs(rootSpanStart);
        log.info("[OBS_TIMING][T1] root span started: traceId={}, rootSpanId={}, conversationId={}, ts={}",
                traceId, rootSpanId, command.getConversationId(), rootSpanStart);

        try {
            agentChatChainService.proceed(context);
            log.info("[OBS_TIMING][T6] chain returned before stream complete: traceId={}, rootSpanId={}, streamDispatchStarted={}, ts={}",
                    traceId, rootSpanId, context.isStreamDispatchStarted(), System.currentTimeMillis());
        } catch (Exception e) {
            throw e;
        }

    }
}

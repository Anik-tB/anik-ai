package com.aianik.anik.ai.feature.agent.chain;

import com.aianik.anik.ai.persistence.admin.po.UserPO;
import com.aianik.anik.ai.persistence.agent.mapper.AgentMapper;
import com.aianik.anik.ai.persistence.agent.po.AgentPO;
import com.aianik.anik.ai.persistence.security.UserSessionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Initialization: Load user and agent
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class InitContextHandler implements AgentChatHandler {

    private final AgentMapper agentMapper;

    @Override
    public void handle(AgentChatContext ctx) {
        UserPO user = ctx.getRequestUser() != null ? ctx.getRequestUser() : UserSessionUtils.currentUserSession();
        AgentPO agent = agentMapper.selectById(ctx.getAgentId());
        if (agent == null) {
            try {
                ctx.getEmitter().send("Error: Agent not found: " + ctx.getAgentId(), MediaType.TEXT_PLAIN);
                ctx.getEmitter().complete();
            } catch (IOException e) {
                log.error("Failed to write error message", e);
            }
            ctx.setTerminated(true);
            return;
        }

        ctx.setUser(user);
        ctx.setAgent(agent);
        // traceId and rootSpanId have been generated and set by AgentChatService
        // Only the context_preparation SPAN ID is generated here for subsequent Handler to record sub-observations.
        ctx.setContextPreparationSpanId(UUID.randomUUID().toString());
    }
}

package com.aianik.anik.ai.admin.service.agent;

import com.aianik.anik.ai.feature.agent.chain.AgentChatContext;
import com.aianik.anik.ai.feature.agent.chain.AgentChatHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Responsibility chain executor, holds an ordered list of Handlers, and drives them one by one through proceed
 */
@Component
public class AgentChatChainService {
    private final List<AgentChatHandler> handlers;
    public AgentChatChainService(List<AgentChatHandler> handlers) {
        this.handlers = handlers;
    }

    public void proceed(AgentChatContext ctx) {
        for (AgentChatHandler handler : handlers) {
            handler.handle(ctx);
        }
    }
}

package com.aizuda.anik.ai.feature.agent.chain;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * System prompt word initialization: use agent instruction as the basis of systemPrompt
 */
@Component
@Order(50)
public class SystemPromptHandler implements AgentChatHandler {

    @Override
    public void handle(AgentChatContext ctx) {
        if (ctx.isTerminated()) {
            return;
        }
        String instruction = ctx.getAgent().getInstruction();
        ctx.setSystemPrompt(instruction != null ? instruction : "You are a helpful AI assistant.");
    }
}

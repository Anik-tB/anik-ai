package com.aizuda.anik.ai.feature.agent.chain;

/**
 * Agent dialogue responsibility chain Handler interface
 * <p>After each Handler completes its responsibilities, it calls {@code chain.proceed(ctx)} to pass control to the next one;
 * If a short-circuit is required (such as verification failure or early error return), return directly without calling proceed.
 */
public interface AgentChatHandler {

    void handle(AgentChatContext ctx);
}

package com.aizuda.anik.ai.agent.common.context;

import io.micrometer.context.ThreadLocalAccessor;

/**
 * Register AgentChatContextHolder (ThreadLocal) to Reactor's context-propagation mechanism.
 *
 * After using it with Hooks.enableAutomaticContextPropagation(),
 * Reactor will automatically capture the ChatContext of the current thread when subscribing.
 * And resume when each operator switches threads, thus ensuring that Advisor and ObservationHandler
 * The callback can get the value through AgentChatContextHolder.getContext() on any thread.
 */
public class AgentChatContextThreadLocalAccessor
        implements ThreadLocalAccessor<AgentChatContextHolder.ChatContext> {

    public static final String KEY = "AgentChatContext";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public AgentChatContextHolder.ChatContext getValue() {
        return AgentChatContextHolder.getContext();
    }

    @Override
    public void setValue(AgentChatContextHolder.ChatContext value) {
        AgentChatContextHolder.setContext(value);
    }

    @Override
    public void setValue() {
        AgentChatContextHolder.clear();
    }
}

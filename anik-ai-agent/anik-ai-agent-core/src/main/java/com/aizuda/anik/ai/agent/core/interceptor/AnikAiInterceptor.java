package com.aizuda.anik.ai.agent.core.interceptor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.core.Ordered;

/**
 * The client-side LLM calls the interceptor SPI.
 */
public interface AnikAiInterceptor extends Ordered {

    default ChatClientRequest beforeRequest(ChatClientRequest request) {
        return request;
    }

    default ChatClientResponse afterResponse(ChatClientResponse response) {
        return response;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}

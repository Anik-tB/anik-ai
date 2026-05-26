package com.aizuda.anik.ai.agent.core.interceptor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

import java.util.Comparator;
import java.util.List;

/**
 * Interceptor chain executor
 * <p>
 * Managing the ordered execution of a {@link AnikAiInterceptor} list:
 * <ul>
 *   <li>{@code applyBefore}: Execute {@code beforeRequest} in order according to Order</li>
 *   <li>{@code applyAfter}: Execute {@code afterResponse} in reverse order of Order</li>
 * </ul>
 *
 * @author openanik
 */
public class AnikAiInterceptorChain {

    private final List<AnikAiInterceptor> interceptors;

    public AnikAiInterceptorChain(List<AnikAiInterceptor> interceptors) {
        this.interceptors = interceptors == null ? List.of() : interceptors.stream()
                .sorted(Comparator.comparingInt(AnikAiInterceptor::getOrder))
                .toList();
    }

    /**
     * Execute the beforeRequest of all interceptors in order
     */
    public ChatClientRequest applyBefore(ChatClientRequest request) {
        ChatClientRequest current = request;
        for (AnikAiInterceptor interceptor : interceptors) {
            current = interceptor.beforeRequest(current);
        }
        return current;
    }

    /**
     * Execute afterResponse of all interceptors in reverse order
     */
    public ChatClientResponse applyAfter(ChatClientResponse response) {
        ChatClientResponse current = response;
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            current = interceptors.get(i).afterResponse(current);
        }
        return current;
    }

    public List<AnikAiInterceptor> getInterceptors() {
        return interceptors;
    }

    public boolean isEmpty() {
        return interceptors.isEmpty();
    }
}

package com.aizuda.anik.ai.agent.core.advisor;

import com.aizuda.anik.ai.agent.core.interceptor.AnikAiInterceptor;
import com.aizuda.anik.ai.agent.core.interceptor.AnikAiInterceptorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

import java.util.List;

/**
 * Advisor wrapper that implements the {@link AnikAiInterceptor} chain.
 * <p>
 * Delegate to {@link AnikAiInterceptorChain} to execute before (forward sequence) / after (reverse sequence).
 *
 * @author openanik
 */
public class InterceptorChainAdvisor implements BaseAdvisor {

    private final AnikAiInterceptorChain chain;

    public InterceptorChainAdvisor(List<AnikAiInterceptor> interceptors) {
        this.chain = new AnikAiInterceptorChain(interceptors);
    }

    @Override
    public String getName() {
        return "InterceptorChainAdvisor";
    }

    @Override
    public int getOrder() {
        return 300;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        return chain.applyBefore(request);
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        return chain.applyAfter(response);
    }
}

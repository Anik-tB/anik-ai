package com.aizuda.anik.ai.agent.core.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.metadata.Usage;
import reactor.core.publisher.Flux;

/**
 * Extract the Token usage from the final chunk of the streaming response and write it into {@link ClientStreamExecutionContext}.
 * <p>
 * Model configurationenable {@code streamUsage(true)} is required so that OpenAI will return usage data in the final SSE chunk.
 *
 * @author openanik
 * @date 2026-04-20
 */
public class TokenUsageCollectorAdvisor implements StreamAdvisor {

    @Override
    public String getName() {
        return "TokenUsageCollectorAdvisor";
    }

    @Override
    public int getOrder() {
        return 350;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        Object st = request.context().get(ClientAdvisorKeys.STREAM_STATE);
        if (!(st instanceof ClientStreamExecutionContext state)) {
            return chain.nextStream(request);
        }

        return chain.nextStream(request)
                .doOnNext(response -> extractUsage(response, state));
    }

    private void extractUsage(ChatClientResponse response, ClientStreamExecutionContext state) {
        ChatResponse cr = response.chatResponse();
        if (cr == null) {
            return;
        }
        Usage usage = cr.getMetadata().getUsage();
        if (usage.getTotalTokens() > 0) {
            state.setPromptTokens(usage.getPromptTokens());
            state.setCompletionTokens(usage.getCompletionTokens());
        }
    }
}

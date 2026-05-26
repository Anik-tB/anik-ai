package com.aizuda.anik.ai.agent.core.advisor;

import com.aizuda.anik.ai.agent.core.executor.ClientChatExecutor;
import lombok.Setter;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * The cumulative status of a single streaming call (written by the Advisor and converted to {@link ClientChatExecutor.ChatCompletionResult} on completion).
 */
public class ClientStreamExecutionContext {

    public final StringBuilder fullText = new StringBuilder();
    public final StringBuilder thinkingText = new StringBuilder();
    public final long startTime = System.currentTimeMillis();

    /** Accumulated tool call list (collected step by step in stream mode)*/
    private final List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();

    /** Token usage in the final streaming chunk */
    @Setter
    private int promptTokens;
    @Setter
    private int completionTokens;

    public void addToolCall(AssistantMessage.ToolCall toolCall) {
        if (toolCall != null && !containsToolCall(toolCall.id())) {
            toolCalls.add(toolCall);
        }
    }

    public void addToolCalls(List<AssistantMessage.ToolCall> calls) {
        if (calls != null) {
            calls.forEach(this::addToolCall);
        }
    }

    public List<AssistantMessage.ToolCall> getToolCalls() {
        return new ArrayList<>(toolCalls);
    }

    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }

    private boolean containsToolCall(String id) {
        return toolCalls.stream().anyMatch(tc -> tc.id().equals(id));
    }

    public ClientChatExecutor.ChatCompletionResult toCompletionResult() {
        long duration = System.currentTimeMillis() - startTime;
        return new ClientChatExecutor.ChatCompletionResult(
                fullText.toString(), thinkingText.toString(), promptTokens, completionTokens, duration);
    }
}

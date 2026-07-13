package com.aianik.anik.ai.agent.core.executor;

import com.aianik.anik.ai.agent.common.context.AgentChatContextHolder;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * ToolCallback decorator: dynamically inject traceId and current TOOL observationId into ToolContext each time it is called.
 * Enable the MCP Server to obtain the trace context through the ToolContext and accurately link the sub-observations to the corresponding TOOL.
 */
public class TracingToolCallbackWrapper implements ToolCallback {

    private final ToolCallback delegate;

    public TracingToolCallbackWrapper(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolArguments) {
        return delegate.call(toolArguments);
    }

    @Override
    public String call(String toolArguments, ToolContext toolContext) {
        AgentChatContextHolder.ChatContext ctx = AgentChatContextHolder.getContext();
        if (ctx != null && ctx.getTraceId() != null) {
            Map<String, Object> enriched = new HashMap<>(
                    toolContext != null ? toolContext.getContext() : Map.of());
            enriched.put("traceId", ctx.getTraceId());
            enriched.put("parentToolObservationId", ctx.getCurrentToolObservationId());
            toolContext = new ToolContext(enriched);
        }
        return delegate.call(toolArguments, toolContext);
    }
}

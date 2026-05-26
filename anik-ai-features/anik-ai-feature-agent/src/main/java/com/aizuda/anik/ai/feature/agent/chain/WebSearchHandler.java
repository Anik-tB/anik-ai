package com.aizuda.anik.ai.feature.agent.chain;

import com.aizuda.anik.ai.common.websearch.WebSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Internet search: If agentenables webSearch, inject WebSearchTool into the tool callback list.
 * It is up to LLM to call decision making independently when needed.
 */
@Slf4j
//@Component
//@Order(65)
public class WebSearchHandler implements AgentChatHandler {

    @Value("${anik-ai.web-search.tavily.api-key:}")
    private String tavilyApiKey;

    @Value("${anik-ai.web-search.max-results:5}")
    private int maxResults;

    @Override
    public void handle(AgentChatContext ctx) {
        if (!Boolean.TRUE.equals(ctx.getAgent().getWebSearchEnabled())) {
            return;
        }
        if (tavilyApiKey == null || tavilyApiKey.isBlank()) {
            log.warn("webSearchEnabled=true but anik-ai.web-search.tavily.api-key is not configured, skip online search, agentId={}", ctx.getAgentId());
            return;
        }

        List<ToolCallback> callbacks = new ArrayList<>(ctx.getToolCallbacks());
        callbacks.addAll(Arrays.asList(ToolCallbacks.from(new WebSearchTool(tavilyApiKey, maxResults))));
        ctx.getToolCallbacks().addAll(callbacks);
    }
}

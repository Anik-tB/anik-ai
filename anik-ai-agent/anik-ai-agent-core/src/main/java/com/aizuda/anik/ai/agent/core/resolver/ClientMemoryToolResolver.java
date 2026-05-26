package com.aizuda.anik.ai.agent.core.resolver;

import com.aizuda.anik.ai.common.dto.agent.ChatDispatchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * Client-side memory retrieval tool parser
 * <p>
 * Memory tools have been simplified - no more separate tool registration, memory retrieval is handled in the background
 */
@Slf4j
public class ClientMemoryToolResolver {

    public ClientMemoryToolResolver() {
    }

    public List<ToolCallback> resolve(ChatDispatchRequest request) {
        // Memory tools are now handled by the backend without independent tool registration
        return List.of();
    }
}

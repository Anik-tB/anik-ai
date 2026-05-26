package com.aizuda.anik.ai.agent.core.resolver;

import com.aizuda.anik.ai.agent.common.rpc.RpcClient;
import com.aizuda.anik.ai.agent.core.tool.RagSearchTool;
import com.aizuda.anik.ai.common.dto.agent.ChatDispatchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;
import java.util.List;

/**
 * Client-side RAG knowledge base search tool parser
 * <p>
 * RAG Tools Simplified - No more separate tool registration, RAG search functionality is handled in the background
 */
@Slf4j
public class ClientRagToolResolver {
    private final RpcClient rpcClient;

    public ClientRagToolResolver(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public List<ToolCallback> resolve(ChatDispatchRequest request) {
        ChatDispatchRequest.AgentConfig agentConfig = request.getAgentConfig();
        if (agentConfig == null
                || !Boolean.TRUE.equals(agentConfig.getRagEnabled())
                || agentConfig.getRagId() == null) {
            return List.of();
        }

        log.info("RAG tool resolved: ragId={}", agentConfig.getRagId());
        return Arrays.asList(ToolCallbacks.from(
                new RagSearchTool(agentConfig.getRagId(), rpcClient)
        ));
    }
}

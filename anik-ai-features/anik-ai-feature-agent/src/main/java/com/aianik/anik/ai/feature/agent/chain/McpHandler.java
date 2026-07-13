package com.aianik.anik.ai.feature.agent.chain;


import com.aianik.anik.ai.common.mcp.McpServerRef;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.model.service.McpToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.aianik.anik.ai.common.dto.agent.ChatDispatchRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP loading: If agentenables MCP, load the bound MCP server list and prepare {@link ChatDispatchRequest.McpServerDescriptor} for remote distribution.
 */
@Slf4j
@Component
@Order(40)
@RequiredArgsConstructor
public class McpHandler implements AgentChatHandler {

    private final McpToolService mcpToolService;

    @Override
    public void handle(AgentChatContext ctx) {
        if (ctx.isTerminated()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        List<ChatDispatchRequest.McpServerDescriptor> descriptors = new ArrayList<>();

        // Load the MCP server configured in the database
        if (Boolean.TRUE.equals(ctx.getAgent().getMcpEnabled())) {
            var servers = mcpToolService.getMcpServersForAgent(ctx.getAgentId());
            List<Long> disabledIds = ctx.getDisabledMcpServerIds();
            if (disabledIds != null && !disabledIds.isEmpty()) {
                servers = servers.stream()
                        .filter(s -> !disabledIds.contains(s.getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
            ctx.setMcpServers(servers);
            for (McpServerRef s : servers) {
                descriptors.add(toDescriptor(s));
            }
        }

        ctx.setMcpServerDescriptors(descriptors);
    }

    private static ChatDispatchRequest.McpServerDescriptor toDescriptor(McpServerRef s) {
        return ChatDispatchRequest.McpServerDescriptor.builder()
                .id(s.getId())
                .name(s.getName())
                .transportType(s.getTransportType())
                .baseUri(s.getBaseUri())
                .endpoint(s.getEndpoint())
                .command(s.getCommand())
                .args(parseJsonList(s.getArgs()))
                .envVars(parseJsonMap(s.getEnvVars()))
                .authType(s.getAuthType())
                .authConfig(parseJsonMap(s.getAuthConfig()))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return JsonUtil.parseObject(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private static Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return JsonUtil.parseHashMap(json);
        } catch (Exception e) {
            return Map.of();
        }
    }


}

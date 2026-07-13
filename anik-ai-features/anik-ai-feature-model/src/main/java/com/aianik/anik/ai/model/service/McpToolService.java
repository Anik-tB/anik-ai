package com.aianik.anik.ai.model.service;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.common.enums.mcp.McpTransportTypeEnum;
import com.aianik.anik.ai.common.execption.AnikAiException;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.persistence.agent.mapper.AgentMcpServerMapper;
import com.aianik.anik.ai.persistence.mcp.mapper.McpServerMapper;
import com.aianik.anik.ai.persistence.agent.po.AgentMcpServerPO;
import com.aianik.anik.ai.common.mcp.McpServerRef;
import com.aianik.anik.ai.persistence.mcp.po.McpServerPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MCP tool service: dynamically connect to MCP server, discover tools, and convert to Spring AI ToolCallback
 * Supports three transmission methods: SSE, Streamable HTTP, Stdio
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolService {

    private final McpServerMapper mcpServerMapper;
    private final AgentMcpServerMapper agentMcpServerMapper;

    /**
     * Cache Connected MCP Client (key=serverId)
     */
    private final Map<Long, McpSyncClient> clientCache = new ConcurrentHashMap<>();

    /**
     * Get the list of MCP servers associated with the agent
     */
    public List<McpServerPO> getMcpServersForAgent(Long agentId) {
        List<AgentMcpServerPO> relations = agentMcpServerMapper.selectList(
                new LambdaQueryWrapper<AgentMcpServerPO>().eq(AgentMcpServerPO::getAgentId, agentId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> serverIds = relations.stream()
                .map(AgentMcpServerPO::getMcpServerId)
                .collect(Collectors.toList());
        return mcpServerMapper.selectByIds(serverIds);
    }

    /**
     * Get all available ToolCallbacks based on the MCP server list.
     */
    public List<ToolCallback> getToolCallbacks(List<? extends McpServerRef> mcpServers) {
        List<McpSyncClient> clients = new ArrayList<>();

        for (McpServerRef server : mcpServers) {
            try {
                McpSyncClient client = getOrCreateClient(server);
                clients.add(client);
            } catch (Exception e) {
                log.warn("Failed to connect MCP server: {} (id={}), error: {}",
                        server.getName(), server.getId(), e.getMessage());
            }
        }

        if (clients.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            SyncMcpToolCallbackProvider provider = SyncMcpToolCallbackProvider.builder()
                    .mcpClients(clients)
                    .build();
            ToolCallback[] callbacks = provider.getToolCallbacks();
            return Arrays.asList(callbacks);
        } catch (Exception e) {
            log.error("Failed to get tool callbacks from MCP servers", e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtain or create MCP Client (with cache) and select different connection methods according to the transmission type.
     */
    private McpSyncClient getOrCreateClient(McpServerRef server) {
        return clientCache.computeIfAbsent(server.getId(), id -> {
            Integer transportType = server.getTransportType();
            if (transportType == null) {
                transportType = McpTransportTypeEnum.SSE.getValue();
            }
            McpTransportTypeEnum transportEnum = McpTransportTypeEnum.fromValue(transportType);
            String protocol = transportEnum != null ? transportEnum.getProtocol() : McpTransportTypeEnum.SSE.getProtocol();

            return switch (protocol) {
                case "stdio" -> createStdioClient(server);
                case "streamable_http" -> createStreamableHttpClient(server);
                case "sse" -> createSseClient(server);
                default -> throw new AnikAiException("Unsupported transport type: " + protocol);
            };
        });
    }

    /**
     * Create MCP Client for Stdio transmission
     */
    private McpSyncClient createStdioClient(McpServerRef server) {
        String command = server.getCommand();
        if (StrUtil.isBlank(command)) {
            throw new AnikAiException("Stdio transport requires a command for MCP server: " + server.getName());
        }

        ServerParameters.Builder paramsBuilder = ServerParameters.builder(command);

        //Parse parameter list
        if (StrUtil.isNotBlank(server.getArgs())) {
            List<String> args = JsonUtil.parseObject(server.getArgs(), new TypeReference<List<String>>() {});
            if (args != null && !args.isEmpty()) {
                paramsBuilder.args(args);
            }
        }

        // Parse environment variables
        if (StrUtil.isNotBlank(server.getEnvVars())) {
            Map<String, String> envVars = JsonUtil.parseObject(server.getEnvVars(), new TypeReference<Map<String, String>>() {});
            if (envVars != null && !envVars.isEmpty()) {
                paramsBuilder.env(envVars);
            }
        }

        ServerParameters params = paramsBuilder.build();
        JacksonMcpJsonMapperSupplier supplier = new JacksonMcpJsonMapperSupplier();
        McpJsonMapper jsonMapper = supplier.get();
        StdioClientTransport transport = new StdioClientTransport(params, jsonMapper);
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        log.info("MCP client connected via Stdio to: {} (command: {})", server.getName(), command);
        return client;
    }

    /**
     * Create MCP Client for Streamable HTTP transport
     */
    private McpSyncClient createStreamableHttpClient(McpServerRef server) {
        String baseUri = server.getBaseUri();
        String endpoint = server.getEndpoint();

        if (StrUtil.isNotBlank(baseUri)) {
            HttpClientStreamableHttpTransport.Builder builder = HttpClientStreamableHttpTransport.builder(baseUri);
            if (StrUtil.isNotBlank(endpoint)) {
                builder.endpoint(endpoint);
            }
            McpSyncClient client = McpClient.sync(builder.build()).build();
            client.initialize();
            log.info("MCP client connected via Streamable HTTP to: {}{}", baseUri, endpoint);
            return client;
        }

        if (StrUtil.isBlank(endpoint)) {
            throw new AnikAiException("Streamable HTTP transport requires baseUri or endpoint for MCP server: " + server.getName());
        }

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(endpoint).build();
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        log.info("MCP client connected via Streamable HTTP to: {}", endpoint);
        return client;
    }

    /**
     * Create an MCP Client for SSE transport
     */
    private McpSyncClient createSseClient(McpServerRef server) {
        String baseUri = server.getBaseUri();
        String endpoint = server.getEndpoint();

        if (StrUtil.isNotBlank(baseUri)) {
            HttpClientSseClientTransport.Builder builder = HttpClientSseClientTransport.builder(baseUri);
            if (StrUtil.isNotBlank(endpoint)) {
                builder.sseEndpoint(endpoint);
            }
            McpSyncClient client = McpClient.sync(builder.build()).build();
            client.initialize();
            log.info("MCP client connected via SSE to: {}{}", baseUri, endpoint);
            return client;
        }

        if (StrUtil.isBlank(endpoint)) {
            throw new AnikAiException("SSE transport requires baseUri or endpoint for MCP server: " + server.getName());
        }

        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(endpoint).build();
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        log.info("MCP client connected via SSE to: {}", endpoint);
        return client;
    }

    /**
     * Clear cached connections for a server
     */
    public void clearClientCache(Long serverId) {
        McpSyncClient client = clientCache.remove(serverId);
        if (client != null) {
            try {
                client.closeGracefully();
            } catch (Exception e) {
                log.warn("Failed to close MCP client for server: {}", serverId, e);
            }
        }
    }

    /**
     * Clear all cached connections
     */
    public void clearAllClientCache() {
        clientCache.forEach((id, client) -> {
            try {
                client.closeGracefully();
            } catch (Exception e) {
                log.warn("Failed to close MCP client for server: {}", id, e);
            }
        });
        clientCache.clear();
    }
}

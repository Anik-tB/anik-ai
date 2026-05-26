package com.aizuda.anik.ai.agent.core.resolver;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.dto.agent.ChatDispatchRequest;
import com.aizuda.anik.ai.common.enums.mcp.McpTransportTypeEnum;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.json.McpJsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;

import java.util.*;

/**
 * Client-side MCP tool parser
 * <p>
 * Connects to the MCP service from the mcpServers descriptor list in dispatch data and returns a ToolCallback.
 *
 * @author openanik
 * @date 2025-04-08
 */
@Slf4j
public class ClientMcpToolResolver {

    private final List<McpSyncClient> connectedClients = new ArrayList<>();

    /**
     * Parse the MCP descriptor list, connect to the MCP service and return the tool callback
     */
    public List<ToolCallback> resolve(List<ChatDispatchRequest.McpServerDescriptor> mcpServers) {
        List<ToolCallback> callbacks = new ArrayList<>();

        if (mcpServers == null || mcpServers.isEmpty()) {
            return callbacks;
        }

        for (ChatDispatchRequest.McpServerDescriptor desc : mcpServers) {
            try {
                connectAndRegisterServer(desc, callbacks);
            } catch (Exception e) {
                log.warn("Failed to connect MCP server: {}", desc.getName(), e);
            }
        }

        return callbacks;
    }

    /**
     * Close all connections
     * Todo needs to be cached here, it doesn’t have to be closed every time
     */
    public void close() {
        for (McpSyncClient client : connectedClients) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Failed to close MCP client", e);
            }
        }
        connectedClients.clear();
    }

    private void connectAndRegisterServer(ChatDispatchRequest.McpServerDescriptor desc,
                                          List<ToolCallback> callbacks) {
        Integer transportType = desc.getTransportType() != null
                ? desc.getTransportType()
                : McpTransportTypeEnum.SSE.getValue();
        McpSyncClient client = connectMcpServer(desc, transportType);
        if (client != null) {
            connectedClients.add(client);
            registerToolCallbacks(client, callbacks);
            log.info("MCP server connected: name={}, type={}", desc.getName(), transportType);
        }
    }

    private void registerToolCallbacks(McpSyncClient client, List<ToolCallback> callbacks) {
        SyncMcpToolCallbackProvider provider = SyncMcpToolCallbackProvider.builder()
                .mcpClients(List.of(client))
                .build();
        callbacks.addAll(Arrays.asList(provider.getToolCallbacks()));
    }

    private McpSyncClient connectMcpServer(ChatDispatchRequest.McpServerDescriptor desc, Integer transportType) {
        return switch (transportType) {
            case 1 -> connectSseClient(desc);
            case 2 -> connectStreamableHttp(desc);
            case 3 -> connectStdioClient(desc);
            default -> {
                log.warn("Unsupported MCP transport: {}", transportType);
                yield null;
            }
        };
    }

    private McpSyncClient connectStreamableHttp(ChatDispatchRequest.McpServerDescriptor desc) {
        String baseUri = desc.getBaseUri();
        String endpoint = desc.getEndpoint();

        if (StrUtil.isNotBlank(baseUri)) {
            HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(baseUri)
                    .endpoint(endpoint).build();
            McpSyncClient client = McpClient.sync(transport).build();
            client.initialize();
            return client;
        }

        if (StrUtil.isBlank(endpoint)) {
            log.warn("Streamable HTTP endpoint is blank for server: {}", desc.getName());
            return null;
        }

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(endpoint).build();
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        return client;
    }

    private McpSyncClient connectSseClient(ChatDispatchRequest.McpServerDescriptor desc) {
        String baseUri = desc.getBaseUri();
        String endpoint = desc.getEndpoint();

        if (StrUtil.isNotBlank(baseUri)) {
            HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(baseUri)
                    .sseEndpoint(endpoint).build();
            McpSyncClient client = McpClient.sync(transport).build();
            client.initialize();
            return client;
        }

        if (StrUtil.isBlank(endpoint)) {
            log.warn("SSE endpoint is blank for server: {}", desc.getName());
            return null;
        }

        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(endpoint).build();
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        return client;
    }

    private McpSyncClient connectStdioClient(ChatDispatchRequest.McpServerDescriptor desc) {
        String command = desc.getCommand();
        if (StrUtil.isBlank(command)) {
            log.warn("Stdio command is blank for server: {}", desc.getName());
            return null;
        }

        ServerParameters params = buildServerParameters(desc, command);
        StdioClientTransport transport = createStdioTransport(params);
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        return client;
    }

    private ServerParameters buildServerParameters(ChatDispatchRequest.McpServerDescriptor desc, String command) {
        ServerParameters.Builder paramsBuilder = ServerParameters.builder(command);

        addArgsIfPresent(paramsBuilder, desc.getArgs());
        addEnvVarsIfPresent(paramsBuilder, desc.getEnvVars());

        return paramsBuilder.build();
    }

    private void addArgsIfPresent(ServerParameters.Builder paramsBuilder, List<String> args) {
        if (args != null && !args.isEmpty()) {
            paramsBuilder.args(args);
        }
    }

    private void addEnvVarsIfPresent(ServerParameters.Builder paramsBuilder, Map<String, String> envVars) {
        if (envVars != null && !envVars.isEmpty()) {
            paramsBuilder.env(envVars);
        }
    }

    private StdioClientTransport createStdioTransport(ServerParameters params) {
        JacksonMcpJsonMapperSupplier supplier = new JacksonMcpJsonMapperSupplier();
        McpJsonMapper jsonMapper = supplier.get();
        return new StdioClientTransport(params, jsonMapper);
    }
}

package com.aianik.anik.ai.openapi.client.core.config;

import com.aianik.anik.ai.agent.common.config.AnikAiAgentProperties;
import lombok.Data;

/**
 * OpenAPI client configuration properties
 *
 * @author openanik
 * @date 2026-04-24
 */
@Data
public class AnikAiOpenApiProperties {

    private boolean enabled = false;

    /**
     * It can be not configured by default. If not configured, it will be {@link AnikAiAgentProperties#getServer().gethost()} }
     */
    private String serverHost;
    /**
     * The default is 8080 corresponding to server.port: 8080
     */
    private int webPort = 8080;
    /**
     * Whether it is https protocol
     */
    private boolean https;
    /**
     * common prefix
     */
    private String prefix = "anik-ai";

    private long connectTimeoutMs = 5000;
    private long readTimeoutMs = 60000;
    private long chatTimeoutMs = 300000;
}

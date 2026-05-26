package com.aizuda.anik.ai.persistence.mcp.po;

import com.aizuda.anik.ai.common.mcp.McpServerRef;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MCP (Model Context Protocol) server persistence object
 * Table: anik_ai_mcp_server
 *
 * Represents an external MCP server configuration
 * Supports multiple transmission methods: SSE, HTTP stream, Stdio process
 * Agent can call third-party tools and resources through MCP
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_mcp_server")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class McpServerPO implements McpServerRef {

    /**
     * MCP server ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Server name
     * Used for front-end display and user identification
     */
    private String name;

    /**
     * Server description
     * Functional description of the MCP server
     */
    private String description;

    /**
     * Transmission type
     * SSE: Server Sent Events (One-way flow)
     * STREAMABLE_HTTP: HTTP stream (bidirectional)
     * STDIO: Local process/Shell command
     */
    private Integer transportType;

    /**
     * Service base address
     * Only used when transportType is SSE or STREAMABLE_HTTP
     * Format: http://host:port
     */
    private String baseUri;

    /**
     * Service endpoint path
     * Only used when transportType is SSE or STREAMABLE_HTTP
     * Format: /sse or /mcp
     */
    private String endpoint;

    /**
     * Stdio command
     * Only used when transportType is STDIO
     * Executable command or script path
     * Example: /usr/bin/python, /home/user/mcp-server.sh
     */
    private String command;

    /**
     * Stdio commandparameter (JSON array format)
     * Only used when transportType is STDIO
     * Example: ["-m", "mcp_module", "--debug"]
     */
    private String args;

    /**
     * Stdio environment variables (JSON object format)
     * Only used when transportType is STDIO
     * Environment variables set when executing the command
     * Example: {"API_KEY": "xxx", "LOG_LEVEL": "DEBUG"}
     */
    private String envVars;

    /**
     * MCP protocol version
     * Implemented MCP protocol version number
     * For example: 1.0, 2.0
     */
    private String version;

    /**
     * Certification type
     * NONE: No certification
     * API_KEY: API key authentication
     * OAUTH2: OAuth2 authentication
     * CUSTOM: Custom certification
     */
    private Integer authType;

    /**
     * Authentication configuration (JSON format)
     * Store corresponding authentication information according to authType
     * API_KEY example: {"key": "sk-xxx"}
     * OAUTH2 example: {"client_id": "...", "client_secret": "..."}
     */
    private String authConfig;

    /**
     * Server status
     * ACTIVE: active/normal
     * INACTIVE: inactive/Disabled
     * ERROR: Error/Connection failed
     * DISCONNECTED: Disconnected
     */
    private Integer status;

    /**
     * Capabilities supported by MCP server (JSON array format)
     * Define the tools and resources supported by this server
     * Example: ["tools", "resources", "prompts"]
     */
    private String capabilities;

    /**
     * Last connection time
     * The last time a successful connection was made to the MCP server
     * for health checks and monitoring
     */
    private LocalDateTime lastConnectDt;

    /**
     * Creator userID (foreign key)
     * Linked to anik_ai_user.id
     * The person who created the configuration for this MCP server
     */
    private Long creatorId;

    /**
     * creation time
     * The moment the MCP server configuration is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time the MCP server configured a refresh
     */
    private LocalDateTime updateDt;
}

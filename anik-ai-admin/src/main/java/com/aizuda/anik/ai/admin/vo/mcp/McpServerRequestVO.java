package com.aizuda.anik.ai.admin.vo.mcp;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class McpServerRequestVO {

    private String name;

    private String description;

    /**
     * Transport type: sse/streamable_http/stdio
     */
    private Integer transportType;

    private String baseUri;

    private String endpoint;

    /**
     * stdio command
     */
    private String command;

    /**
     * stdio commandparameter
     */
    private List<String> args;

    /**
     * Stdio environment variables
     */
    private Map<String, String> envVars;

    private Integer authType;

    private Map<String, Object> authConfig;

    private List<String> capabilities;
}

package com.aizuda.anik.ai.common.enums.mcp;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MCP connection status
 */
@Getter
@AllArgsConstructor
public enum McpConnectionStatusEnum {

    DISCONNECTED(0, "Not connected"),
    CONNECTED(1, "Connected"),
    ERROR(2, "abnormal");

    @EnumValue
    private final Integer value;
    private final String description;
}

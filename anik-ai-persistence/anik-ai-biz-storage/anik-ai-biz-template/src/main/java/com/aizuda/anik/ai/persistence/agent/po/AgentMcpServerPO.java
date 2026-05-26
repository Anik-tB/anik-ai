package com.aizuda.anik.ai.persistence.agent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent associates persistent objects with MCP server
 * Table: anik_ai_agent_mcp_server
 *
 * Represents the many-to-many relationship between Agent and MCP Server
 * An Agent can bind multiple MCP Servers to enhance the Agent's capabilities
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_agent_mcp_server")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentMcpServerPO {

    /**
     * Association ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent ID (foreign key)
     * Linked to anik_ai_agent.id
     */
    private Long agentId;

    /**
     * MCP server ID (foreign key)
     * Linked to anik_ai_mcp_server.id
     */
    private Long mcpServerId;

    /**
     * creation time
     * The moment when the association is created
     */
    private LocalDateTime createDt;
}

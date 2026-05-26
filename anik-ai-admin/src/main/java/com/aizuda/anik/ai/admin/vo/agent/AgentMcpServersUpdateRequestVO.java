package com.aizuda.anik.ai.admin.vo.agent;

import lombok.Data;

import java.util.List;

@Data
public class AgentMcpServersUpdateRequestVO {

    private List<Long> mcpServerIds;
}

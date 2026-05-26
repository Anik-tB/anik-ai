package com.aizuda.anik.ai.openapi.service;

import com.aizuda.anik.ai.common.enums.agent.AgentStatusEnum;
import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiAgentVO;
import com.aizuda.anik.ai.persistence.agent.mapper.AgentMapper;
import com.aizuda.anik.ai.persistence.agent.po.AgentPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenAPI Agent query service
 *
 * @author openanik
 * @date 2026-04-24
 */
@Service
@RequiredArgsConstructor
public class OpenApiAgentService {

    private final AgentMapper agentMapper;

    public List<OpenApiAgentVO> listAgents() {
        List<AgentPO> agents = agentMapper.selectList(
                new LambdaQueryWrapper<AgentPO>()
                        .eq(AgentPO::getStatus, AgentStatusEnum.ACTIVE.getStatus())
                        .orderByDesc(AgentPO::getUpdateDt));

        return agents.stream().map(this::toVO).collect(Collectors.toList());
    }

    public OpenApiAgentVO getAgent(Long agentId) {
        AgentPO agent = agentMapper.selectById(agentId);
        if (agent == null) {
            throw new AnikAiException("Agent does not exist: " + agentId);
        }
        return toVO(agent);
    }

    private OpenApiAgentVO toVO(AgentPO po) {
        return OpenApiAgentVO.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .avatar(po.getAvatar())
                .greeting(po.getGreeting())
                .status(po.getStatus())
                .build();
    }
}

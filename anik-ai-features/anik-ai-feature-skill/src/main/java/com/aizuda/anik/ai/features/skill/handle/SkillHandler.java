package com.aizuda.anik.ai.features.skill.handle;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.persistence.agent.mapper.AgentSkillMapper;
import com.aizuda.anik.ai.persistence.agent.po.AgentSkillPO;
import com.aizuda.anik.ai.persistence.skill.mapper.SkillFileMapper;
import com.aizuda.anik.ai.persistence.skill.mapper.SkillMapper;
import com.aizuda.anik.ai.persistence.skill.po.SkillFilePO;
import com.aizuda.anik.ai.persistence.skill.po.SkillPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Skill service interface (infrastructure layer)
 * Error 500 (Server Error)!!1500.That’s an error.There was an error. Please try again later.That’s all we know.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SkillHandler {

    private final SkillMapper skillMapper;
    private final AgentSkillMapper agentSkillMapper;

    /**
     * Get the Skill associated with the agent (including skillContent) for dialogue injection
     */
    public List<SkillPO> getSkillsWithContentForAgent(Long agentId) {
        List<AgentSkillPO> relations = agentSkillMapper.selectList(
                new LambdaQueryWrapper<AgentSkillPO>().eq(AgentSkillPO::getAgentId, agentId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> skillIds = relations.stream()
                .map(AgentSkillPO::getSkillId)
                .collect(Collectors.toList());
        return skillMapper.selectByIds(skillIds);
    }

}

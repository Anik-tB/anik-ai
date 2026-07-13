package com.aianik.anik.ai.feature.agent.chain;

import com.aianik.anik.ai.common.enums.agent.StatusEnum;
import com.aianik.anik.ai.persistence.agent.enums.ConversationRoleEnum;
import com.aianik.anik.ai.persistence.agent.mapper.AgentConversationMapper;
import com.aianik.anik.ai.persistence.agent.mapper.AgentConversationRecordMapper;
import com.aianik.anik.ai.persistence.agent.po.AgentConversationPO;
import com.aianik.anik.ai.persistence.agent.po.AgentConversationRecordPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Session management: Create or reuse conversation records, persist User messages
 */
@Component
@Order(20)
@RequiredArgsConstructor
public class ConversationHandler implements AgentChatHandler {

    private final AgentConversationMapper conversationMapper;
    private final AgentConversationRecordMapper recordMapper;

    @Override
    public void handle(AgentChatContext ctx) {
        if (ctx.isTerminated()) {
            return;
        }
        Long agentId = ctx.getAgentId();
        String conversationId = ctx.getConversationId();
        String content = ctx.getContent();
        Long userId = ctx.getUser().getId();

        Long convCount = conversationMapper.selectCount(
                new LambdaQueryWrapper<AgentConversationPO>()
                        .eq(AgentConversationPO::getConversationId, conversationId));
        if (convCount == 0) {
            conversationMapper.insert(AgentConversationPO.builder()
                    .agentId(agentId)
                    .userId(userId)
                    .conversationId(conversationId)
                    .title(content.substring(0, Math.min(content.length(), 16)))
                    .build());
        }

        recordMapper.insert(AgentConversationRecordPO.builder()
                .agentId(agentId)
                .conversationId(conversationId)
                .userId(userId)
                .role(ConversationRoleEnum.USER.getValue())
                .content(content)
                .status(StatusEnum.RUNNING.getValue())
                .build());

    }
}

package com.aianik.anik.ai.feature.agent.callback;

import com.aianik.anik.ai.common.dto.agent.ConversationCreateRequest;
import com.aianik.anik.ai.common.enums.agent.StatusEnum;
import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aianik.anik.ai.common.grpc.constant.UriConstants;
import com.aianik.anik.ai.common.grpc.handler.GrpcHandlerRequest;
import com.aianik.anik.ai.common.grpc.handler.GrpcRequestHandler;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.persistence.agent.enums.ConversationRoleEnum;
import com.aianik.anik.ai.persistence.agent.mapper.AgentConversationMapper;
import com.aianik.anik.ai.persistence.agent.mapper.AgentConversationRecordMapper;
import com.aianik.anik.ai.persistence.agent.po.AgentConversationPO;
import com.aianik.anik.ai.persistence.agent.po.AgentConversationRecordPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Callback: Create conversation + save User messages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationCreateCallbackHandler implements GrpcRequestHandler {

    private final AgentConversationMapper conversationMapper;
    private final AgentConversationRecordMapper recordMapper;

    @Override
    public boolean supports(String uri) {
        return UriConstants.CALLBACK_CONVERSATION_CREATE.equals(uri);
    }

    @Override
    public GrpcAnikAiResult handle(GrpcHandlerRequest request) {
        try {
            ConversationCreateRequest req = JsonUtil.parseObject(request.getBody(), ConversationCreateRequest.class);

            // Create conversation if it does not exist
            long count = conversationMapper.selectCount(
                    new LambdaQueryWrapper<AgentConversationPO>()
                            .eq(AgentConversationPO::getConversationId, req.getConversationId()));
            if (count == 0) {
                String content = req.getUserMessage();
                String title = content != null && content.length() > 16
                        ? content.substring(0, 16) : content;
                conversationMapper.insert(AgentConversationPO.builder()
                        .agentId(req.getAgentId()).userId(req.getUserId())
                        .conversationId(req.getConversationId()).title(title)
                        .build());
            }

            //Save User messages
            recordMapper.insert(AgentConversationRecordPO.builder()
                    .agentId(req.getAgentId()).conversationId(req.getConversationId()).userId(req.getUserId())
                    .role(ConversationRoleEnum.USER.getValue())
                    .content(req.getUserMessage())
                    .status(StatusEnum.RUNNING.getValue())
                    .build());

            return buildSuccess();
        } catch (Exception e) {
            log.error("Callback conversation create failed", e);
            return buildError(e.getMessage());
        }
    }

    private GrpcAnikAiResult buildSuccess() {
        return GrpcAnikAiResult.newBuilder().setStatus(1).setMessage("OK").build();
    }

    private GrpcAnikAiResult buildError(String msg) {
        return GrpcAnikAiResult.newBuilder().setStatus(0).setMessage(msg).build();
    }
}

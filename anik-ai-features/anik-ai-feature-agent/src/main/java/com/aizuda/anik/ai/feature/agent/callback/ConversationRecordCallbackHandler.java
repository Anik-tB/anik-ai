package com.aizuda.anik.ai.feature.agent.callback;

import com.aizuda.anik.ai.common.dto.agent.ConversationRecordRequest;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.constant.UriConstants;
import com.aizuda.anik.ai.common.grpc.handler.GrpcHandlerRequest;
import com.aizuda.anik.ai.common.grpc.handler.GrpcRequestHandler;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.persistence.agent.mapper.AgentConversationRecordMapper;
import com.aizuda.anik.ai.persistence.agent.po.AgentConversationRecordPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Callback: Save conversation message (user/assistant)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationRecordCallbackHandler implements GrpcRequestHandler {

    private final AgentConversationRecordMapper recordMapper;

    @Override
    public boolean supports(String uri) {
        return UriConstants.CALLBACK_CONVERSATION_RECORD.equals(uri);
    }

    @Override
    public GrpcAnikAiResult handle(GrpcHandlerRequest request) {
        try {
            ConversationRecordRequest req = JsonUtil.parseObject(request.getBody(), ConversationRecordRequest.class);

            recordMapper.insert(AgentConversationRecordPO.builder()
                    .agentId(req.getAgentId())
                    .conversationId(req.getConversationId())
                    .userId(req.getUserId())
                    .role(req.getRole())
                    .content(req.getContent())
                    .build());

            return GrpcAnikAiResult.newBuilder().setStatus(1).setMessage("OK").build();
        } catch (Exception e) {
            log.error("Callback conversation record failed", e);
            return GrpcAnikAiResult.newBuilder().setStatus(0).setMessage(e.getMessage()).build();
        }
    }
}

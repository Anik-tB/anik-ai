package com.aizuda.anik.ai.agent.core.grpc.handler;

import com.aizuda.anik.ai.agent.common.counter.ActiveChatCounter;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.constant.UriConstants;
import com.aizuda.anik.ai.common.grpc.handler.GrpcHandlerRequest;
import com.aizuda.anik.ai.common.grpc.handler.GrpcRequestHandler;
import com.aizuda.anik.ai.common.util.JsonUtil;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Client side Ping:{@link UriConstants#PING}
 */
@RequiredArgsConstructor
public class PingRequestHandler implements GrpcRequestHandler {

    private static final int REQUEST_STATUS_SUCCESS = 1;

    private final ActiveChatCounter activeChatCounter;

    @Override
    public boolean supports(String uri) {
        return UriConstants.PING.equals(uri);
    }

    @Override
    public GrpcAnikAiResult handle(GrpcHandlerRequest request) {
        PingResponse response = PingResponse.builder()
                .timestamp(System.currentTimeMillis())
                .activeChats(activeChatCounter.get())
                .build();
        
        return GrpcAnikAiResult.newBuilder()
                .setStatus(REQUEST_STATUS_SUCCESS)
                .setData(JsonUtil.toJsonString(response))
                .build();
    }

    /**
     * Ping response data
     */
    @Data
    @Builder
    private static class PingResponse {
        private Long timestamp;
        private Integer activeChats;
    }
}

package com.aizuda.anik.ai.agent.core;

import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiRequest;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.handler.GrpcRequestDispatcher;
import io.grpc.stub.ServerCalls;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Client-side gRPC request dispatcher — delegate {@link GrpcRequestDispatcher} Route by URI.
 */
@RequiredArgsConstructor
public class ClientRequestDispatcher {

    private final GrpcRequestDispatcher grpcRequestDispatcher;

    /**
     * Unary handler (such as /ping)
     */
    public ServerCalls.UnaryMethod<GrpcAnikAiRequest, GrpcAnikAiResult> unaryHandler() {
        return (request, observer) -> {
            String uri = request.getMetadata().getUri();
            Map<String, String> headers = request.getMetadata().getHeadersMap();
            GrpcAnikAiResult result = grpcRequestDispatcher.dispatchUnary(
                    request.getReqId(), uri, headers, request.getBody());
            observer.onNext(result);
            observer.onCompleted();
        };
    }

    /**
     * Server-Streaming handler (such as /chat/dispatch)
     */
    public ServerCalls.ServerStreamingMethod<GrpcAnikAiRequest, GrpcAnikAiResult> streamingHandler() {
        return (request, observer) -> {
            String uri = request.getMetadata().getUri();
            Map<String, String> headers = request.getMetadata().getHeadersMap();
            grpcRequestDispatcher.dispatchStreaming(
                    uri, headers, request.getBody(), request.getReqId(), observer);
        };
    }
}

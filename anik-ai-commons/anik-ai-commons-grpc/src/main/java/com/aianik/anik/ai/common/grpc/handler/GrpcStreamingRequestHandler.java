package com.aianik.anik.ai.common.grpc.handler;

import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import io.grpc.stub.StreamObserver;

/**
 * gRPC Server-Streaming request handler.
 *
 * @author openanik
 */
public interface GrpcStreamingRequestHandler {

    boolean supports(String uri);

    void handle(GrpcHandlerRequest request, StreamObserver<GrpcAnikAiResult> observer);
}

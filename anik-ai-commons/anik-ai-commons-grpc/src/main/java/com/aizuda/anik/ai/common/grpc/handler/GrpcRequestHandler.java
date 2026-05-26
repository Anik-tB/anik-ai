package com.aizuda.anik.ai.common.grpc.handler;

import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;

/**
 * gRPC Unary Request Handler — Policy interface for routing by URI.
 *
 * @author openanik
 */
public interface GrpcRequestHandler {

    boolean supports(String uri);

    GrpcAnikAiResult handle(GrpcHandlerRequest request);
}

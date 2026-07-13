package com.aianik.anik.ai.common.grpc.handler;

import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;

/**
 * Response constructs common to the gRPC distribution layer.
 */
public final class GrpcDispatchResults {

    private static final int STATUS_FAILED = 0;

    private GrpcDispatchResults() {
    }

    public static GrpcAnikAiResult unknownUri(long reqId, String uri) {
        return GrpcAnikAiResult.newBuilder()
                .setReqId(reqId)
                .setStatus(STATUS_FAILED)
                .setMessage("Unknown URI: " + uri)
                .build();
    }

    public static GrpcAnikAiResult unknownUri(String uri) {
        return GrpcAnikAiResult.newBuilder()
                .setStatus(STATUS_FAILED)
                .setMessage("Unknown URI: " + uri)
                .build();
    }
}

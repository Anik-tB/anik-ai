package com.aizuda.anik.ai.common.grpc.client;

import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiRequest;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.auto.Metadata;
import com.aizuda.anik.ai.common.grpc.constant.GrpcConstants;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * gRPC client call utility
 */
@Slf4j
public final class GrpcChannelUtil {

    private static final AtomicLong REQ_ID_GEN = new AtomicLong(0);

    private GrpcChannelUtil() {}

    /**
     * Send Unary request (synchronous blocking, default CallOptions)
     */
    public static GrpcAnikAiResult sendUnary(ManagedChannel channel, String uri, String body,
                                               Map<String, String> headers) {
        return sendUnary(channel, uri, body, headers, CallOptions.DEFAULT);
    }

    /**
     * Send Unary request (synchronous blocking, custom CallOptions)
     */
    public static GrpcAnikAiResult sendUnary(ManagedChannel channel, String uri, String body,
                                               Map<String, String> headers, CallOptions callOptions) {
        GrpcAnikAiRequest request = buildRequest(uri, body, headers);

        MethodDescriptor<GrpcAnikAiRequest, GrpcAnikAiResult> md =
                buildMethodDescriptor(MethodDescriptor.MethodType.UNARY,
                        GrpcConstants.UNARY_SERVICE_NAME, GrpcConstants.UNARY_METHOD_NAME);

        return ClientCalls.blockingUnaryCall(
                channel.newCall(md, callOptions), request);
    }

    /**
     * Send Server Streaming request (asynchronous callback)
     */
    public static void sendServerStreaming(ManagedChannel channel, String uri, String body,
                                           Map<String, String> headers,
                                           StreamObserver<GrpcAnikAiResult> responseObserver) {
        GrpcAnikAiRequest request = buildRequest(uri, body, headers);

        MethodDescriptor<GrpcAnikAiRequest, GrpcAnikAiResult> md =
                buildMethodDescriptor(MethodDescriptor.MethodType.SERVER_STREAMING,
                        GrpcConstants.STREAMING_SERVICE_NAME, GrpcConstants.STREAMING_METHOD_NAME);

        ClientCalls.asyncServerStreamingCall(
                channel.newCall(md, CallOptions.DEFAULT), request, responseObserver);
    }

    private static GrpcAnikAiRequest buildRequest(String uri, String body, Map<String, String> headers) {
        Metadata metadata = Metadata.newBuilder()
                .setUri(uri)
                .putAllHeaders(headers)
                .build();

        return GrpcAnikAiRequest.newBuilder()
                .setReqId(REQ_ID_GEN.incrementAndGet())
                .setMetadata(metadata)
                .setBody(body != null ? body : "")
                .build();
    }

    private static MethodDescriptor<GrpcAnikAiRequest, GrpcAnikAiResult> buildMethodDescriptor(
            MethodDescriptor.MethodType type, String serviceName, String methodName) {
        return MethodDescriptor.<GrpcAnikAiRequest, GrpcAnikAiResult>newBuilder()
                .setType(type)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcAnikAiRequest.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcAnikAiResult.getDefaultInstance()))
                .build();
    }
}

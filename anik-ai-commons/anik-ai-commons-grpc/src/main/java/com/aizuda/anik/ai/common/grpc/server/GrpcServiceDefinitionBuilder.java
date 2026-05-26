package com.aizuda.anik.ai.common.grpc.server;

import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiRequest;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.constant.GrpcConstants;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import lombok.extern.slf4j.Slf4j;

/**
 * gRPC service definition build tool (consistent with anik-job GrpcServer pattern)
 * <p>
 * Provides programmatic construction of both Unary and Server Streaming service definitions.
 */
@Slf4j
public final class GrpcServiceDefinitionBuilder {

    private GrpcServiceDefinitionBuilder() {}

    /**
     * Create Unary service definition
     */
    public static ServerServiceDefinition createUnaryServiceDefinition(
            ServerCalls.UnaryMethod<GrpcAnikAiRequest, GrpcAnikAiResult> unaryMethod) {
        return createUnaryServiceDefinition(
                GrpcConstants.UNARY_SERVICE_NAME,
                GrpcConstants.UNARY_METHOD_NAME,
                unaryMethod);
    }

    public static ServerServiceDefinition createUnaryServiceDefinition(
            String serviceName, String methodName,
            ServerCalls.UnaryMethod<GrpcAnikAiRequest, GrpcAnikAiResult> unaryMethod) {

        MethodDescriptor<GrpcAnikAiRequest, GrpcAnikAiResult> methodDescriptor =
                MethodDescriptor.<GrpcAnikAiRequest, GrpcAnikAiResult>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                        .setRequestMarshaller(ProtoUtils.marshaller(GrpcAnikAiRequest.getDefaultInstance()))
                        .setResponseMarshaller(ProtoUtils.marshaller(GrpcAnikAiResult.getDefaultInstance()))
                        .build();

        return ServerServiceDefinition.builder(serviceName)
                .addMethod(methodDescriptor, ServerCalls.asyncUnaryCall(unaryMethod))
                .build();
    }

    /**
     * Create a Server Streaming service definition
     */
    public static ServerServiceDefinition createServerStreamingServiceDefinition(
            ServerCalls.ServerStreamingMethod<GrpcAnikAiRequest, GrpcAnikAiResult> streamingMethod) {
        return createServerStreamingServiceDefinition(
                GrpcConstants.STREAMING_SERVICE_NAME,
                GrpcConstants.STREAMING_METHOD_NAME,
                streamingMethod);
    }

    public static ServerServiceDefinition createServerStreamingServiceDefinition(
            String serviceName, String methodName,
            ServerCalls.ServerStreamingMethod<GrpcAnikAiRequest, GrpcAnikAiResult> streamingMethod) {

        MethodDescriptor<GrpcAnikAiRequest, GrpcAnikAiResult> methodDescriptor =
                MethodDescriptor.<GrpcAnikAiRequest, GrpcAnikAiResult>newBuilder()
                        .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
                        .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                        .setRequestMarshaller(ProtoUtils.marshaller(GrpcAnikAiRequest.getDefaultInstance()))
                        .setResponseMarshaller(ProtoUtils.marshaller(GrpcAnikAiResult.getDefaultInstance()))
                        .build();

        return ServerServiceDefinition.builder(serviceName)
                .addMethod(methodDescriptor, ServerCalls.asyncServerStreamingCall(streamingMethod))
                .build();
    }
}

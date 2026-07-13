package com.aianik.anik.ai.common.grpc.constant;

/**
 * gRPC service constants
 */
public interface GrpcConstants {

    /** Unary service (heartbeat, result reporting, etc.) */
    String UNARY_SERVICE_NAME = "UnaryRequest";
    String UNARY_METHOD_NAME = "unaryRequest";

    /** Server Streaming service (Chat distribution, streaming return chunks) */
    String STREAMING_SERVICE_NAME = "ServerStreamingRequest";
    String STREAMING_METHOD_NAME = "serverStreamingRequest";

    /** Default Server gRPC port */
    int DEFAULT_SERVER_GRPC_PORT = 1789;

    /** Default Client gRPC port */
    int DEFAULT_CLIENT_GRPC_PORT = 1790;
}

package com.aianik.anik.ai.agent.common.rpc;

import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiRequest;
import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aianik.anik.ai.common.grpc.server.GrpcServiceDefinitionBuilder;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.ServerCalls;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Client-side gRPC server (receives dispatch request from Server)
 * <p>
 * Register two services: Unary (Ping) + Server Streaming (DispatchChat)
 */
@Slf4j
public class ClientGrpcServer {

    private Server server;
    private final int port;
    private final ServerCalls.UnaryMethod<GrpcAnikAiRequest, GrpcAnikAiResult> unaryHandler;
    private final ServerCalls.ServerStreamingMethod<GrpcAnikAiRequest, GrpcAnikAiResult> streamingHandler;

    public ClientGrpcServer(int port,
                            ServerCalls.UnaryMethod<GrpcAnikAiRequest, GrpcAnikAiResult> unaryHandler,
                            ServerCalls.ServerStreamingMethod<GrpcAnikAiRequest, GrpcAnikAiResult> streamingHandler) {
        this.port = port;
        this.unaryHandler = unaryHandler;
        this.streamingHandler = streamingHandler;
    }

    public void start() throws Exception {
        server = NettyServerBuilder.forPort(port)
                .addService(GrpcServiceDefinitionBuilder.createUnaryServiceDefinition(unaryHandler))
                .addService(GrpcServiceDefinitionBuilder.createServerStreamingServiceDefinition(streamingHandler))
                .maxInboundMessageSize(10 * 1024 * 1024)
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .permitKeepAliveTime(5, TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(true)
                .build()
                .start();

        log.info("Client gRPC server started on port {}", port);
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
            try {
                server.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("Client gRPC server stopped");
    }
}

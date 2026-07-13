package com.aianik.anik.ai.rpc;

import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiRequest;
import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aianik.anik.ai.common.grpc.handler.GrpcRequestDispatcher;
import com.aianik.anik.ai.common.grpc.server.GrpcServiceDefinitionBuilder;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Server-side gRPC server (receives client-side heartbeat and result reporting)
 *
 * @author openanik
 * @date 2025-04-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnikAiServerGrpcServer implements SmartLifecycle {

    /** gRPC maximum inbound message size (bytes) */
    private static final int GRPC_MAX_INBOUND_MESSAGE_SIZE = 10 * 1024 * 1024;

    /** gRPC keepalive time (seconds) */
    private static final int GRPC_KEEP_ALIVE_TIME_SECONDS = 30;

    /** gRPC keepalive timeout (seconds)*/
    private static final int GRPC_KEEP_ALIVE_TIMEOUT_SECONDS = 10;

    /** gRPC allowed keepalive time (minutes)*/
    private static final int GRPC_PERMIT_KEEP_ALIVE_MINUTES = 5;

    /** Close wait time (seconds) */
    private static final int SHUTDOWN_AWAIT_SECONDS = 10;

    /** Life cycle stage */
    private static final int LIFECYCLE_PHASE = Integer.MAX_VALUE - 100;

    private final GrpcRequestDispatcher grpcRequestDispatcher;

    @Value("${anik-ai.server.grpc-port:1789}")
    private int grpcPort;

    private Server server;
    private volatile boolean running = false;

    @Override
    public void start() {
        try {
            server = buildGrpcServer();
            server.start();
            running = true;
            log.info("Anik-AI Server gRPC started on port {}", grpcPort);
        } catch (Exception e) {
            log.error("Failed to start Server gRPC on port {}", grpcPort, e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
            try {
                if (!server.awaitTermination(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        running = false;
        log.info("Anik-AI Server gRPC stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return LIFECYCLE_PHASE;
    }

    private Server buildGrpcServer() {
        return NettyServerBuilder.forPort(grpcPort)
                .addService(GrpcServiceDefinitionBuilder.createUnaryServiceDefinition(
                        this::handleUnaryRequest))
                .maxInboundMessageSize(GRPC_MAX_INBOUND_MESSAGE_SIZE)
                .keepAliveTime(GRPC_KEEP_ALIVE_TIME_SECONDS, TimeUnit.SECONDS)
                .keepAliveTimeout(GRPC_KEEP_ALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .permitKeepAliveTime(GRPC_PERMIT_KEEP_ALIVE_MINUTES, TimeUnit.MINUTES)
                .permitKeepAliveWithoutCalls(true)
                .build();
    }

    private void handleUnaryRequest(GrpcAnikAiRequest request, StreamObserver<GrpcAnikAiResult> observer) {
        String uri = request.getMetadata().getUri();
        Map<String, String> headers = request.getMetadata().getHeadersMap();

        try {
            GrpcAnikAiResult result = grpcRequestDispatcher.dispatchUnary(
                    request.getReqId(), uri, headers, request.getBody());
            observer.onNext(result);
            observer.onCompleted();
        } catch (Exception e) {
            log.error("Error handling gRPC request uri={}", uri, e);
            observer.onNext(buildErrorResult(request.getReqId(), e.getMessage()));
            observer.onCompleted();
        }
    }

    private GrpcAnikAiResult buildErrorResult(long reqId, String errorMessage) {
        return GrpcAnikAiResult.newBuilder()
                .setReqId(reqId)
                .setStatus(0)
                .setMessage(errorMessage != null ? errorMessage : "Unknown error")
                .build();
    }
}

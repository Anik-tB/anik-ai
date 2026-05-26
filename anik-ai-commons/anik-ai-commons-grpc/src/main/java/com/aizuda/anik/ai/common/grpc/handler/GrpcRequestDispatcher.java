package com.aizuda.anik.ai.common.grpc.handler;

import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * gRPC request unified dispatcher - shared by Server / Client, routed by {@link GrpcRequestHandler#supports(String)}.
 *
 * @author openanik
 */
@Component
@RequiredArgsConstructor
public class GrpcRequestDispatcher {

    private final List<GrpcRequestHandler> unaryHandlers;
    private final List<GrpcStreamingRequestHandler> streamingHandlers;

    public GrpcAnikAiResult dispatchUnary(long reqId, String uri, Map<String, String> headers, String body) {
        GrpcHandlerRequest request = GrpcHandlerRequest.builder()
                .reqId(reqId).uri(uri).headers(headers).body(body).build();

        for (GrpcRequestHandler handler : unaryHandlers) {
            if (handler.supports(uri)) {
                return handler.handle(request).toBuilder().setReqId(reqId).build();
            }
        }
        return GrpcDispatchResults.unknownUri(reqId, uri);
    }

    public void dispatchStreaming(String uri, Map<String, String> headers, String body,
                                  long reqId, StreamObserver<GrpcAnikAiResult> observer) {
        GrpcHandlerRequest request = GrpcHandlerRequest.builder()
                .reqId(reqId).uri(uri).headers(headers).body(body).build();

        for (GrpcStreamingRequestHandler handler : streamingHandlers) {
            if (handler.supports(uri)) {
                handler.handle(request, observer);
                return;
            }
        }
        observer.onNext(GrpcDispatchResults.unknownUri(reqId, uri));
        observer.onCompleted();
    }
}

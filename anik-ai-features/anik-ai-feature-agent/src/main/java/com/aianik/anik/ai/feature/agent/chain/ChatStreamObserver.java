package com.aianik.anik.ai.feature.agent.chain;

import com.aianik.anik.ai.common.dto.agent.ChatStreamResponse;
import com.aianik.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.feature.agent.persist.ChatResultPersistCommand;
import com.aianik.anik.ai.feature.agent.persist.ChatResultPersistService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * gRPC streaming response observer: bridges Agent gRPC streaming to the front-end HTTP Emitter and persists the results on completion.
 */
@Slf4j
public class ChatStreamObserver implements StreamObserver<GrpcAnikAiResult> {

    private final AgentChatContext context;
    private final ChatResultPersistService persistService;
    private final int shortTermWindow;

    public ChatStreamObserver(AgentChatContext context,
                              ChatResultPersistService persistService,
                              int shortTermWindow) {
        this.context = context;
        this.persistService = persistService;
        this.shortTermWindow = shortTermWindow;
    }

    @Override
    public void onNext(GrpcAnikAiResult result) {
        try {
            ChatStreamResponse data = JsonUtil.parseObject(result.getData(), ChatStreamResponse.class);
            if (data.getType() == null) {
                return;
            }

            switch (data.getType()) {
                case ChatStreamResponse.TYPE_TEXT -> handleText(data);
                case ChatStreamResponse.TYPE_THINKING -> handleThinking(data);
                case ChatStreamResponse.TYPE_COMPLETION -> handleCompletion(data);
                case ChatStreamResponse.TYPE_ERROR -> handleError(data);
                default -> { /* ignore unknown */ }
            }
        } catch (Exception e) {
            log.warn("Failed to proxy chunk to emitter", e);
        }
    }

    @Override
    public void onError(Throwable t) {
        log.error("gRPC stream error from client", t);
        try {
            context.getEmitter().send("[ERROR] Client disconnected: " + t.getMessage());
        } catch (Exception ignored) {
        }
        context.getEmitter().complete();
    }

    @Override
    public void onCompleted() {
        context.getEmitter().complete();
    }

    private void handleText(ChatStreamResponse data) throws Exception {
        String json = JsonUtil.toJsonString(ChatStreamResponse.text(data.getContent()));
        context.getEmitter().send(json + "\n");
    }

    private void handleThinking(ChatStreamResponse data) throws Exception {
        String text = data.getContent();
        if (text == null || text.isEmpty()) {
            return;
        }
        String json = JsonUtil.toJsonString(ChatStreamResponse.thinking(text));
        context.getEmitter().send(json + "\n");
    }

    private void handleCompletion(ChatStreamResponse data) {
        String fullText = data.getFullText();
        String fullThinking = data.getFullThinking();
        log.info("Chat completed from client: conversationId={}, durationMs={}",
                context.getConversationId(), data.getDurationMs());

        persistService.persistAsync(ChatResultPersistCommand.builder()
                .agentId(context.getAgent().getId())
                .userId(context.getUser().getId())
                .conversationId(context.getConversationId())
                .userName(context.getUser().getUsername())
                .fullText(fullText)
                .thinkingText(fullThinking)
                .memoryEnabled(context.getAgent().getMemoryEnabled())
                .agentModelId(context.getModelId())
                .shortTermWindow(shortTermWindow)
                .build());
    }

    private void handleError(ChatStreamResponse data) throws Exception {
        log.error("Client error: {}", data.getErrorMessage());
        context.getEmitter().send("[ERROR] " + data.getErrorMessage());
    }

}

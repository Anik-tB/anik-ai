package com.aianik.anik.ai.openapi.client.core.listener;

/**
 * SSE event listener interface
 * <p>
 * Used to receive real-time text, thought processes, and completion/error events for streaming conversations.
 *
 * @author openanik
 * @date 2026-04-24
 */
public interface SseEventListener {

    void onText(String text);

    default void onThinking(String thinking) {
    }

    void onComplete(String data);

    void onError(String errorMessage);
}

package com.aizuda.anik.ai.openapi.client.core.rpc;

import com.aizuda.anik.ai.openapi.client.core.listener.SseEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * SSE response stream parser
 * <p>
 * Read the SSE stream line by line, parse event/data pairs, and distribute to SseEventListener.
 *
 * @author openanik
 * @date 2026-04-24
 */
public final class SseResponseHandler {

    private static final Logger log = LoggerFactory.getLogger(SseResponseHandler.class);

    private SseResponseHandler() {
    }

    public static void parse(BufferedReader reader, SseEventListener listener) throws IOException {
        String currentEvent = "text";
        String line;
        StringBuilder dataBuffer = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("event:")) {
                currentEvent = line.substring(6).trim();
            } else if (line.startsWith("data:")) {
                dataBuffer.append(line.substring(5));
            } else if (line.isEmpty()) {
                if (dataBuffer.length() > 0) {
                    dispatchEvent(currentEvent, dataBuffer.toString(), listener);
                    dataBuffer.setLength(0);
                }
                currentEvent = "text";
            }
        }

        if (dataBuffer.length() > 0) {
            dispatchEvent(currentEvent, dataBuffer.toString(), listener);
        }
    }

    private static void dispatchEvent(String event, String data, SseEventListener listener) {
        try {
            switch (event) {
                case "text" -> listener.onText(data);
                case "thinking" -> listener.onThinking(data);
                case "done" -> listener.onComplete(data);
                case "error" -> listener.onError(data);
                default -> log.debug("Unknown SSE event: {}", event);
            }
        } catch (Exception e) {
            log.error("Error dispatching SSE event: {}", event, e);
        }
    }
}

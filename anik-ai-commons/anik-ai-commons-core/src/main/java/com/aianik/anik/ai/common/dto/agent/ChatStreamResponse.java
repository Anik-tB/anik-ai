package com.aianik.anik.ai.common.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat streaming response
 *
 * @author openanik
 * @date 2025-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamResponse {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_THINKING = "thinking";
    public static final String TYPE_COMPLETION = "completion";
    public static final String TYPE_ERROR = "error";

    /** Response type */
    private String type;

    /** Text content */
    private String content;

    /** Full text */
    private String fullText;

    /** Full reasoning process */
    private String fullThinking;

    /** Number of prompt word tokens */
    private Integer promptTokens;

    /** Number of completed tokens */
    private Integer completionTokens;

    /** Execution time (milliseconds) */
    private Long durationMs;

    /** Error code */
    private String errorCode;

    /** Error message */
    private String errorMessage;

    public static ChatStreamResponse text(String text) {
        return ChatStreamResponse.builder()
                .type(TYPE_TEXT)
                .content(text)
                .build();
    }

    public static ChatStreamResponse thinking(String text) {
        return ChatStreamResponse.builder()
                .type(TYPE_THINKING)
                .content(text)
                .build();
    }

    public static ChatStreamResponse completion(String fullText, String fullThinking,
                                                 int promptTokens, int completionTokens, long durationMs) {
        return ChatStreamResponse.builder()
                .type(TYPE_COMPLETION)
                .fullText(fullText)
                .fullThinking(fullThinking)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .durationMs(durationMs)
                .build();
    }

    public static ChatStreamResponse error(String errorCode, String errorMessage) {
        return ChatStreamResponse.builder()
                .type(TYPE_ERROR)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}

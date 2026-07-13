package com.aianik.anik.ai.admin.dto;

import com.aianik.anik.ai.common.dto.agent.ChatStreamResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat flow events (HTTP pushed to frontend in line-delimited JSON format)
 *
 * @author openanik
 * @date 2025-04-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatStreamEvent {

    private String type;
    private String content;

    public static ChatStreamEvent text(String content) {
        return new ChatStreamEvent(ChatStreamResponse.TYPE_TEXT, content);
    }

    public static ChatStreamEvent thinking(String content) {
        return new ChatStreamEvent(ChatStreamResponse.TYPE_THINKING, content);
    }
}

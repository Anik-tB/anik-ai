package com.aizuda.anik.ai.vector.storage.vector.api;

import java.util.Map;
import java.util.function.Function;

/**
 * Vector index name builder: Each scene naming rule is concentrated here, {@link AnikAiVectorStore} only consumes the final string.
 */
public enum IndexNameBuilder {

    /**
     * RAG：{@code rag_{ragId}}
     * parameter：{@code ragId}
     */
    KNOWLEDGE("rag", params -> {
        Long ragId = getLong(params, "ragId");
        return "rag_" + ragId;
    }),

    /**
     * Memory (isolated by Agent dimension): {@code memory_agent_{agentId}}
     * Different userIds are filtered by filterExpression.
     * parameter：{@code agentId}
     */
    MEMORY_AGENT("memory_agent", params -> {
        Long agentId = getLong(params, "agentId");
        return "memory_agent_" + agentId;
    }),

    /**
     * User portrait (extended example):{@code profile_user_{userId}}
     */
    PROFILE_USER("profile_user", params -> {
        Long userId = getLong(params, "userId");
        return "profile_user_" + userId;
    }),

    /**
     * Conversation summary (extended example): {@code conversation_summary_{conversationId}}
     */
    CONVERSATION_SUMMARY("conversation_summary", params -> {
        Long conversationId = getLong(params, "conversationId");
        return "conversation_summary_" + conversationId;
    });

    private final String type;
    private final Function<Map<String, Object>, String> builder;

    IndexNameBuilder(String type, Function<Map<String, Object>, String> builder) {
        this.type = type;
        this.builder = builder;
    }

    public String getType() {
        return type;
    }

    /**
     * Generate index names based on parameter Map.
     */
    public String build(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException(type + " Parameter cannot be empty");
        }
        return builder.apply(params);
    }

    public static IndexNameBuilder fromType(String type) {
        for (IndexNameBuilder b : values()) {
            if (b.type.equals(type)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown index type: " + type);
    }

    private static Long getLong(Map<String, Object> params, String key) {
        Object val = params.get(key);
        if (val == null) {
            throw new IllegalArgumentException("parameter " + key + " cannot be empty");
        }
        if (val instanceof Long) {
            return (Long) val;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            return Long.parseLong((String) val);
        }
        throw new IllegalArgumentException("parameter " + key + " Invalid type: " + val.getClass());
    }
}

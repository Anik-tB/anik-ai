package com.aianik.anik.ai.agent.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * agent chat context holder - uses ThreadLocal to store complete context information for a chat session
 * Used to obtain chat-related metadata from ObservationHandler, event listener, Advisor, etc.
 *
 * Contains information:
 * - Identity information: agentId, userId, conversationId
 * - Model information: modelId, modelName, embeddingModelId
 * - User info：userName, department
 * - Memory configuration: memoryEnabled, memoryTopK, embeddingModelId
 * - Other configurations: skillEnabled, mcpEnabled, webSearchEnabled, etc.
 */
public class AgentChatContextHolder {
    public static final String KEY = "AgentChatContext";

    private static final ThreadLocal<ChatContext> contextHolder = ThreadLocal.withInitial(ChatContext::new);

    /**
     * Chat context data class
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatContext {
        // Identity information
        private Long agentId;
        private Long userId;
        private String conversationId;

        //Model information
        private Long modelId;
        private String modelKey;
        private Long embeddingModelId;

        // User info
        private String userName;
        private String department;

        // Agent configuration
        private Boolean memoryEnabled;
        private Boolean skillEnabled;
        private Boolean mcpEnabled;
        private Boolean webSearchEnabled;

        //Memory system configuration
        private Integer memoryTopK;  //Retrieve Top-K memories, default 5
        /** Number of short-term memory sliding windows (Redis/memory multi-round context), default 20*/
        private Integer shortTermMemorySize;
        /** Memory retrieval configuration ID (when present, MemoryRetriever uses the vector library and recall parameters in the configuration) */
        private Long memoryConfigId;
        private String memoryExtractionType;  //SUMMARY or FULL_TEXT
        private Boolean memorySaveAsync;  //Whether to save memory asynchronously

        // Other metadata
        private String agentName;
        private String agentInstruction;

        // Observability Link Trace ID
        /** Current Trace ID (conversation turn) */
        private String traceId;
        /** The root SPAN ID of the entire interaction (passed by the server) */
        private String rootSpanId;
        /** SPAN ID (agent_execution) of the client execution phase*/
        private String agentExecutionSpanId;
        /** Current GENERATION Observation ID (used by ToolCalling as parent) */
        private String currentGenerationId;
        /** The ID of the TOOL Observation currently being executed (written by onStart for ToolContext to propagate to MCP Server)*/
        private String currentToolObservationId;
        /** The current thinking chain content of GENERATION (written by ThinkingCollectorAdvisor, read by ObservationHandler) */
        private String currentThinkingContent;

    }

    /**
     * Set up full chat context
     */
    public static void setContext(ChatContext context) {
        contextHolder.set(context);
    }
    /**
     * Get full chat context
     */
    public static ChatContext getContext() {
        return contextHolder.get();
    }

    /**
     * Clear context - should be called in try-finally
     */
    public static void clear() {
        contextHolder.remove();
    }


}


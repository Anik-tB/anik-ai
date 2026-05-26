package com.aizuda.anik.ai.agent.core.tool;

import com.aizuda.anik.ai.agent.common.context.AgentChatContextHolder;
import com.aizuda.anik.ai.agent.common.rpc.RpcClient;
import com.aizuda.anik.ai.common.dto.rag.RagSearchRequest;
import com.aizuda.anik.ai.common.dto.rag.RagSearchResponse;
import com.aizuda.anik.ai.common.dto.rag.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.*;

/**
 * Client-side local RAG knowledge base search tool
 * <p>
 * Execute retrieval through gRPC callback server and directly access AgentChatContextHolder on the client side
 * Get the real-time observability ID and publish the RETRIEVER observation event.
 */
@Slf4j
public class RagSearchTool {

    private final Long ragId;
    private final RpcClient rpcClient;

    public RagSearchTool(Long ragId, RpcClient rpcClient) {
        this.ragId = ragId;
        this.rpcClient = rpcClient;
    }

    @Tool(name = "rag_search",
            description = "Retrieve relevant reference materials from the knowledge base. "
                    + "Call this tool when the user's question may require professional knowledge, document content, or domain-specific information. "
                    + "Use keywords relevant to the user's question as query parameters. "
                    + "Do not call for general chat, greetings, or questions clearly outside the knowledge base scope.")
    public String search(
            @ToolParam(description = "The user's question or related query") String queryQuestion) {

        if (queryQuestion == null || queryQuestion.trim().isEmpty()) {
            return "No relevant reference materials found";
        }

        log.info("rag_search: ragId={}, query={}", ragId, queryQuestion);

        try {
            AgentChatContextHolder.ChatContext chatCtx = AgentChatContextHolder.getContext();

            RagSearchRequest req = RagSearchRequest.builder()
                    .ragId(ragId)
                    .query(queryQuestion.trim())
                    .parentObservationId(chatCtx != null ? chatCtx.getCurrentToolObservationId() : null)
                    .build();

            RagSearchResponse resp = rpcClient.searchRag(req);
            List<SearchResult> results = resp != null ? resp.getResults() : null;

            if (results == null || results.isEmpty()) {
                return "No relevant reference found.";
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                sb.append(String.format("[%d] %s\n\n", i + 1, results.get(i).getContent()));
            }
            return sb.toString().trim();

        } catch (Exception e) {
            log.error("rag_search failed, ragId={}", ragId, e);
            return "Search failed: " + e.getMessage();
        }
    }

}

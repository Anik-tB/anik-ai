package com.aizuda.anik.ai.features.rag.handle;

import com.aizuda.anik.ai.common.dto.rag.RagSearchRequest;
import com.aizuda.anik.ai.common.dto.rag.RagSearchResponse;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.constant.UriConstants;
import com.aizuda.anik.ai.common.grpc.handler.GrpcHandlerRequest;
import com.aizuda.anik.ai.common.grpc.handler.GrpcRequestHandler;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.features.rag.dto.RagSearchRequestDTO;
import com.aizuda.anik.ai.features.rag.dto.RagSearchResponseDTO;
import com.aizuda.anik.ai.features.rag.service.RagSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Callback: RAG Knowledge Base Search
 * <p>
 * The client-side KnowledgeSearchTool calls this processor through gRPC to perform retrieval.
 * The responsibility for observation lies with the client.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagSearchCallbackHandler implements GrpcRequestHandler {

    private final RagSearchService ragSearchService;

    @Override
    public boolean supports(String uri) {
        return UriConstants.CALLBACK_RAG_SEARCH.equals(uri);
    }

    @Override
    public GrpcAnikAiResult handle(GrpcHandlerRequest request) {
        try {
            RagSearchRequest ragSearchRequest = JsonUtil.parseObject(request.getBody(), RagSearchRequest.class);
            Long ragId = ragSearchRequest.getRagId();
            String query = ragSearchRequest.getQuery();
            if (ragId == null || query == null || query.isBlank()) {
                return buildError("ragId and query are required");
            }

            RagSearchRequestDTO req = new RagSearchRequestDTO();
            req.setRagId(ragId);
            req.setQuery(query);

            RagSearchResponseDTO resp = ragSearchService.search(req);
            RagSearchResponse ragSearchResponse = new RagSearchResponse();
            ragSearchResponse.setResults(resp.getResults());
            return GrpcAnikAiResult.newBuilder()
                    .setStatus(1).setMessage("OK")
                    .setData(JsonUtil.toJsonString(ragSearchResponse))
                    .build();
        } catch (Exception e) {
            log.error("Callback RAG search failed", e);
            return buildError(e.getMessage());
        }
    }

    private GrpcAnikAiResult buildError(String msg) {
        return GrpcAnikAiResult.newBuilder().setStatus(0).setMessage(msg).build();
    }
}

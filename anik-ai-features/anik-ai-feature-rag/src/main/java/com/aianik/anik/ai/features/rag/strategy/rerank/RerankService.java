package com.aianik.anik.ai.features.rag.strategy.rerank;

import com.aianik.anik.ai.common.dto.rag.SearchResult;

import java.util.List;

public interface RerankService {

    /**
     * Reorder search results
     *
     * @param query query text
     * @param candidates list of candidate results (the caller has truncated it according to the "number of entering reorderings")
     * @param rerankOutputTopN The number of items retained after reranking (consistent with the UI "result return quantity", actually min(this value, candidates.size()))
     * @param rerankModelId rearrange model configuration ID
     * @return the rearranged result list
     */
    List<SearchResult> rerank(String query, List<SearchResult> candidates,
                              int rerankOutputTopN, Long rerankModelId);
}

package com.aianik.anik.ai.common.model;

import java.util.List;

/**
 * Rerank API client interface
 * Encapsulate HTTP calls to different rerank service providers
 */
public interface RerankApiClient {

    /**
     * Call the rerank API
     *
     * @param query query text
     * @param documents candidate document list
     * @param topN returns the top N results
     * @return rearranges the result list (descending order by score)
     */
    List<RerankResultItem> rerank(String query, List<String> documents, int topN);

    /**
     * Single rearrangement results
     */
    record RerankResultItem(int index, double score) {
    }
}

package com.aianik.anik.ai.vector.storage.vector.api;

import java.util.List;

/**
 * Vector storage: only accepts explicit {@code indexName} and is not responsible for deriving names (done by {@link IndexNameBuilder}, etc.).
 */
public interface AnikAiVectorStore {

    String getType();

    void add(VectorAddRequest request);

    void delete(String indexName, List<String> ids);

    /**
     * Delete the entire index/collection/logical partition (semantics are implementation-specific).
     */
    void deleteByIndexName(String indexName);

    List<VectorSearchResult> search(VectorSearchRequest request);

    boolean test();
}

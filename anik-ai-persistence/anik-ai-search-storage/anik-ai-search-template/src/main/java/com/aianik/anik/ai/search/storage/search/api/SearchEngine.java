package com.aianik.anik.ai.search.storage.search.api;

import com.aianik.anik.ai.common.dto.rag.SearchResult;

import java.util.List;

public interface SearchEngine {

    void insert(SearchAddRequest request);

    List<SearchResult> search(SearchRequest request);

    void delete(SearchDeleteRequest request);
}

package com.aizuda.anik.ai.search.storage.search.api;

import com.aizuda.anik.ai.common.dto.rag.SearchResult;

import java.util.List;

public interface SearchEngine {

    void insert(SearchAddRequest request);

    List<SearchResult> search(SearchRequest request);

    void delete(SearchDeleteRequest request);
}

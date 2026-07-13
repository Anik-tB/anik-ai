package com.aianik.anik.ai.search.storage.search.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRequest {

    /**
     * Explicit index name; consistent with vector retrieval style, takes precedence over derived fields in filter.
     */
    private String indexName;

    private String queryText;

    private int topK;

    private String filterExpression;

}

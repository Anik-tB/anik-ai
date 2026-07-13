package com.aianik.anik.ai.vector.storage.vector.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VectorSearchRequest {

    /**
     * Index or collection name; takes precedence over ragId derivation in filter when non-null.
     */
    private String indexName;

    private String queryText;

    private float[] queryVector;

    private int topK;

    private Map<String, Object> filter;

    /**
     * Spring AI Filter expression string, such as {@code "agentId == 1 && userId == 2"}.
     * When non-empty, it takes precedence over filter conditions deduced by filter Map and indexName.
     */
    private String filterExpression;
}

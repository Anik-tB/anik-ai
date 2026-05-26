package com.aizuda.anik.ai.vector.storage.vector.elasticsearch;

import lombok.Data;

/**
 * Elasticsearch vector index connection and mapping parameters, from storage instance {@code config} JSON.
 */
@Data
public class ElasticsearchVectorSettings {

    private boolean enabled = true;

    private String host = "localhost";

    private int port = 9200;

    private String scheme = "http";

    private String username;

    private String password;

    /** Actual index name: {indexPrefix}_{ragId} */
    private String indexPrefix = "anik_ai_vector";

    /** cosine | dot_product | l2_norm */
    private String similarity = "cosine";

    private int numCandidates = 100;
}

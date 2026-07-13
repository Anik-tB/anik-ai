package com.aianik.anik.ai.search.storage.search.elasticsearch;

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

    /** actual index*/
    private String indexPrefix = "anik_ai_search";

}

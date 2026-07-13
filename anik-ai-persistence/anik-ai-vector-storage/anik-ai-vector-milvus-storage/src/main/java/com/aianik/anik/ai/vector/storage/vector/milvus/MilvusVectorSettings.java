package com.aianik.anik.ai.vector.storage.vector.milvus;

import lombok.Data;

/**
 * Milvus connection and collection/index parameters, from storage instance {@code config} JSON.
 */
@Data
public class MilvusVectorSettings {

    private boolean enabled = true;

    private String host = "localhost";

    private int port = 19530;

    private String token;

    private String database = "default";

    /** Actual collection: {collectionPrefix}_{ragId} */
    private String collectionPrefix = "anik_rag_vector";

    /** COSINE | L2 | IP */
    private String metricType = "COSINE";

    /** IVF_FLAT | IVF_SQ8 | HNSW | AUTOINDEX */
    private String indexType = "AUTOINDEX";

    private int nlist = 1024;

    private int nprobe = 16;
}

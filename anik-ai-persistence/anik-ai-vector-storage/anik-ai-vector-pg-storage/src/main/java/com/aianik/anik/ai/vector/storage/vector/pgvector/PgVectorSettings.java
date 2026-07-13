package com.aianik.anik.ai.vector.storage.vector.pgvector;

import lombok.Data;

/**
 * The PostgreSQL vector library is connected to the table/index parameter, which is parsed from the storage instance {@code config} JSON (does not depend on application.yml).
 */
@Data
public class PgVectorSettings {

    private boolean enabled = true;

    private String host = "localhost";

    private int port = 5432;

    private String database = "anik_ai";

    private String username = "postgres";

    private String password = "";

    private boolean sslEnabled = false;

    private String sslMode = "disable";

    private int maxPoolSize = 20;

    private int minIdleConnections = 5;

    private long connectionTimeoutMs = 30000;

    private long idleTimeoutMs = 600000;

    private long maxLifetimeMs = 1800000;

    /** Spring AI default table name; if the existing table uses an old table, it needs to be migrated to this table structure */
    private String vectorTableName = "vector_store";

    private int defaultDimension = 1024;

    private boolean hnswIndexEnabled = true;

    private int hnswEfConstruction = 64;

    private int hnswEfSearch = 32;
}

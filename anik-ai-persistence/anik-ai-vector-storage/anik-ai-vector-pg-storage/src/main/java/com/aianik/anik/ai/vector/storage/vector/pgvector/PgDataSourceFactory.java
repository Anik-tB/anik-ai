package com.aianik.anik.ai.vector.storage.vector.pgvector;

import com.aianik.anik.ai.common.execption.AnikAiException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

/**
 * Create a PostgreSQL connection pool by {@link PgVectorSettings} (from the DB storage instance).
 */
@Slf4j
public final class PgDataSourceFactory {

    private PgDataSourceFactory() {
    }

    /**
     * Create an independent Hikari connection pool for a single vector library access (each storage instance has its own connection parameter).
     */
    public static DataSource createDataSource(PgVectorSettings config) {
        if (!config.isEnabled()) {
            throw new IllegalArgumentException("PostgreSQL vector settings are disabled");
        }
        try {
            HikariConfig hikariConfig = new HikariConfig();
            String jdbcUrl = buildJdbcUrl(config);
            hikariConfig.setJdbcUrl(jdbcUrl);
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setMaximumPoolSize(Math.max(2, config.getMaxPoolSize()));
            hikariConfig.setMinimumIdle(Math.max(1, config.getMinIdleConnections()));
            hikariConfig.setConnectionTimeout(config.getConnectionTimeoutMs());
            hikariConfig.setIdleTimeout(config.getIdleTimeoutMs());
            hikariConfig.setMaxLifetime(config.getMaxLifetimeMs());
            hikariConfig.setDriverClassName("org.postgresql.Driver");
            hikariConfig.setPoolName("PgVector-Instance-" + System.identityHashCode(config));
            hikariConfig.setAutoCommit(true);
            hikariConfig.setConnectionTestQuery("SELECT 1");
            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            log.error("Failed to create PostgreSQL DataSource for pgvector", e);
            throw new AnikAiException("Failed to create PostgreSQL DataSource for pgvector, {}", e.getMessage());
        }
    }

    private static String buildJdbcUrl(PgVectorSettings config) {
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(config.getHost()).append(":").append(config.getPort());
        url.append("/").append(config.getDatabase());
        if (config.isSslEnabled()) {
            url.append("?sslmode=").append(config.getSslMode());
        } else {
            url.append("?sslmode=disable");
        }
        return url.toString();
    }
}

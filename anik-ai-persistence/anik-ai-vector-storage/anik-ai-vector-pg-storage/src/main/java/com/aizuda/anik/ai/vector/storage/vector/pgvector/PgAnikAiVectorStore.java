package com.aizuda.anik.ai.vector.storage.vector.pgvector;

import com.aizuda.anik.ai.vector.storage.enums.VectorStoreType;
import com.aizuda.anik.ai.vector.storage.exception.VectorStoreException;
import com.aizuda.anik.ai.vector.storage.vector.core.AbstractAnikAiVectorStore;
import com.aizuda.anik.ai.model.model.embedding.AnikEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;


/**
 * PostgreSQL: single table {@code vector_store} + metadata distinguishes RAG / memory partition;
 * Semantic delete is filtered by {@code ragId} / {@code agentId+userId}, the old table {@code anik_knowledge_chunk_vector} is no longer used.
 */
@Slf4j
public class PgAnikAiVectorStore extends AbstractAnikAiVectorStore {

    private final JdbcTemplate jdbcTemplate;
    private final PgVectorSettings config;

    public PgAnikAiVectorStore(AnikEmbeddingModel anikEmbeddingModel,
                                Integer embeddingDimensions,
                                PgVectorSettings config) {
        super(anikEmbeddingModel, embeddingDimensions);
        this.config = config;
        this.jdbcTemplate = new JdbcTemplate(PgDataSourceFactory.createDataSource(config));
    }

    private PgVectorStore getStore(String indexName) {
        int dim = embeddingDimensions != null ? embeddingDimensions : config.getDefaultDimension();
        var builder = PgVectorStore.builder(jdbcTemplate, springAiEmbeddingModel)
                .initializeSchema(true)
                .dimensions(dim)
                .schemaName("public")
                .vectorTableName(indexName);
        var store = builder.build();
        try {
            store.afterPropertiesSet();
        } catch (Exception e) {
            throw new VectorStoreException("PgVectorStore (Spring AI) initialization failed", e);
        }
        return store;
    }

    @Override
    public String getType() {
        return VectorStoreType.PG_VECTOR.getType();
    }

    @Override
    public void deleteByIndexName(String indexName) {
        if (indexName == null || indexName.isBlank()) {
            throw new VectorStoreException("indexName cannot be empty");
        }

        jdbcTemplate.execute("DROP TABLE IF EXISTS " + indexName);
    }

    @Override
    public boolean test() {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            return conn.isValid(5);
        } catch (Exception e) {
            log.warn("PostgreSQL connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    protected VectorStore getVectorStore(String indexName) {
        return getStore(indexName);
    }

}

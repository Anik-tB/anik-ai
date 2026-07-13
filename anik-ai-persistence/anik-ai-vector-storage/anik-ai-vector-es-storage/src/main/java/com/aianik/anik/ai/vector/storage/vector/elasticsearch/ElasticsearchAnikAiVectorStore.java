package com.aianik.anik.ai.vector.storage.vector.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import co.elastic.clients.transport.Version;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import com.aianik.anik.ai.vector.storage.enums.VectorStoreType;
import com.aianik.anik.ai.vector.storage.exception.VectorStoreException;
import com.aianik.anik.ai.vector.storage.vector.core.AbstractAnikAiVectorStore;
import com.aianik.anik.ai.model.model.embedding.AnikEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.ai.vectorstore.filter.Filter;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.*;

@Slf4j
public class ElasticsearchAnikAiVectorStore extends AbstractAnikAiVectorStore {

    private final static String INDEX_NAME = "test_index";
    private final ElasticsearchVectorSettings config;
    private final Rest5Client rest5Client;
    private final ConcurrentHashMap<String, org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore> cache =
            new ConcurrentHashMap<>();

    public ElasticsearchAnikAiVectorStore(AnikEmbeddingModel anikEmbeddingModel,
                                           Integer embeddingDimensions,
                                           ElasticsearchVectorSettings config) {
        super(anikEmbeddingModel, embeddingDimensions);
        this.config = config;
        this.rest5Client = buildRest5Client(config);
    }

    @Override
    public String getType() {
        return VectorStoreType.ELASTICSEARCH.getType();
    }

    private ElasticsearchVectorStore getStore(String indexName) {
        return cache.computeIfAbsent(indexName, idx -> {
            ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
            options.setIndexName(idx);
            options.setDimensions(embeddingDimensions);
            options.setSimilarity(parseSimilarity(config.getSimilarity()));

            var store = ElasticsearchVectorStore
                    .builder(rest5Client, springAiEmbeddingModel)
                    .options(options)
                    .initializeSchema(true)
                    .batchingStrategy(new TokenCountBatchingStrategy())
                    .build();
            try {
                store.afterPropertiesSet();
            } catch (Exception e) {
                throw new VectorStoreException("ElasticsearchVectorStore initialization failed: " + idx, e);
            }
            return store;
        });
    }

    private static SimilarityFunction parseSimilarity(String s) {
        if (s == null || s.isBlank()) {
            return SimilarityFunction.cosine;
        }
        try {
            return SimilarityFunction.valueOf(s.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            return SimilarityFunction.cosine;
        }
    }

    private static Rest5Client buildRest5Client(ElasticsearchVectorSettings config) {
        try {
            String uri = config.getScheme() + "://" + config.getHost() + ":" + config.getPort();
            var builder = Rest5Client.builder(URI.create(uri));
            if (config.getUsername() != null && !config.getUsername().isBlank()
                    && config.getPassword() != null) {
                String raw = config.getUsername() + ":" + config.getPassword();
                String b64 = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
                Header[] headers = new Header[]{new BasicHeader("Authorization", "Basic " + b64)};
                builder.setDefaultHeaders(headers);
            }
            return builder.build();
        } catch (Exception e) {
            throw new VectorStoreException("Building Elasticsearch Rest5Client failed", e);
        }
    }

    @Override
    public void delete(String indexName, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        if (indexName == null || indexName.isBlank()) {
            throw new VectorStoreException("indexName cannot be empty");
        }
        getStore(indexName).delete(ids);
    }

    @Override
    public void deleteByIndexName(String indexName) {
        if (indexName == null || indexName.isBlank()) {
            throw new VectorStoreException("indexName cannot be empty");
        }
        cache.remove(indexName);
        try {
            ElasticsearchClient elasticsearchClient = (ElasticsearchClient) getStore(indexName).getNativeClient().get();
            if (elasticsearchClient.indices().exists(e -> e.index(indexName)).value()) {
                elasticsearchClient.indices().delete(d -> d.index(indexName));
                log.info("Deleted Elasticsearch index: {}", indexName);
            }
        } catch (IOException e) {
            throw new VectorStoreException("Failed to delete ES index: " + indexName, e);
        }
    }

    @Override
    public boolean test() {
        String version = Version.VERSION == null ? "Unknown" : Version.VERSION.toString();
        ElasticsearchClient elasticsearchClient =
                new ElasticsearchClient(new Rest5ClientTransport(rest5Client, new Jackson3JsonpMapper()))
                        .withTransportOptions(t -> t.addHeader("user-agent", "spring-ai elastic-java/" + version));
        try {
            elasticsearchClient.info();
            if (elasticsearchClient.indices().exists(e -> e.index(INDEX_NAME)).value()) {
                elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
            }
            return true;
        } catch (IOException e) {
            log.warn("Elasticsearch connection test failed: {}", e.getMessage());
            return false;
        }
    }


    @Override
    protected VectorStore getVectorStore(String indexName) {
        return getStore(indexName);
    }
}

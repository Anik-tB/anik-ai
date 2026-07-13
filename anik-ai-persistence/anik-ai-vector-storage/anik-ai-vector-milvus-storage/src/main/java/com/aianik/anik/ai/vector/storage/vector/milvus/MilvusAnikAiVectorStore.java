package com.aianik.anik.ai.vector.storage.vector.milvus;

import com.aianik.anik.ai.vector.storage.enums.VectorStoreType;
import com.aianik.anik.ai.vector.storage.exception.VectorStoreException;
import com.aianik.anik.ai.vector.storage.vector.core.AbstractAnikAiVectorStore;
import com.aianik.anik.ai.model.model.embedding.AnikEmbeddingModel;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.CheckHealthResponse;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.DropCollectionParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hdfs.protocol.proto.DataTransferProtos;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MilvusAnikAiVectorStore extends AbstractAnikAiVectorStore {

    private final MilvusServiceClient milvusClient;
    private final MilvusVectorSettings config;
    private final ConcurrentHashMap<String, MilvusVectorStore> cache = new ConcurrentHashMap<>();

    public MilvusAnikAiVectorStore(AnikEmbeddingModel anikEmbeddingModel,
                                    Integer embeddingDimensions,
                                    MilvusVectorSettings config) {
        super(anikEmbeddingModel, embeddingDimensions);
        this.config = config;
        this.milvusClient = createClient(config);
    }

    private static MilvusServiceClient createClient(MilvusVectorSettings config) {
        ConnectParam.Builder cb = ConnectParam.newBuilder()
                .withHost(config.getHost())
                .withPort(config.getPort())
                .withDatabaseName(config.getDatabase());
        if (config.getToken() != null && !config.getToken().isBlank()) {
            cb.withAuthorization(config.getToken());
        }
        return new MilvusServiceClient(cb.build());
    }

    @Override
    public String getType() {
        return VectorStoreType.MILVUS.getType();
    }

    private MilvusVectorStore getStore(String collectionName) {
        return cache.computeIfAbsent(collectionName, name -> {
            var builder = MilvusVectorStore.builder(milvusClient, springAiEmbeddingModel)
                    .databaseName(config.getDatabase())
                    .collectionName(name)
                    .embeddingDimension(embeddingDimensions)
                    .metricType(parseMetricType(config.getMetricType()))
                    .indexType(parseIndexType(config.getIndexType()))
                    .indexParameters("{\"nlist\":" + config.getNlist() + "}")
                    .autoId(false)
                    .initializeSchema(true)
                    .batchingStrategy(new TokenCountBatchingStrategy());
            var store = builder.build();
            try {
                store.afterPropertiesSet();
            } catch (Exception e) {
                throw new VectorStoreException("MilvusVectorStore initialization failed: " + name, e);
            }
            return store;
        });
    }

    private static MetricType parseMetricType(String s) {
        if (s == null || s.isBlank()) {
            return MetricType.COSINE;
        }
        try {
            return MetricType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MetricType.COSINE;
        }
    }

    private static IndexType parseIndexType(String s) {
        if (s == null || s.isBlank()) {
            return IndexType.IVF_FLAT;
        }
        String u = s.trim().toUpperCase();
        if ("AUTOINDEX".equals(u)) {
            return IndexType.IVF_FLAT;
        }
        try {
            return IndexType.valueOf(u);
        } catch (IllegalArgumentException e) {
            return IndexType.IVF_FLAT;
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
        R<?> r = milvusClient.dropCollection(
                DropCollectionParam.newBuilder().withCollectionName(indexName).build());
        if (r.getException() != null) {
            throw new VectorStoreException("Milvus dropCollection failed: " + r.getException().getMessage(),
                    r.getException());
        }
        log.info("Dropped Milvus collection: {}", indexName);
    }

    @Override
    public boolean test() {
        R<CheckHealthResponse> checkHealthResponseR = milvusClient.checkHealth();
        return checkHealthResponseR.getStatus() == DataTransferProtos.Status.SUCCESS.getNumber();
    }


    @Override
    protected VectorStore getVectorStore(String indexName) {
        return getStore(indexName);
    }
}

package com.aianik.anik.ai.vector.storage.vector.pgvector;

import com.aianik.anik.ai.common.Lifecycle;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.vector.storage.enums.VectorStoreType;
import com.aianik.anik.ai.vector.storage.vector.VectorStoreFactory;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2026-04-02
 */
@Component
public class PgVectorLifecycle implements Lifecycle {

    @Override
    public void start() {
        VectorStoreFactory.REGISTER.put(VectorStoreType.PG_VECTOR, configDTO ->
                new PgAnikAiVectorStore(
                        configDTO.getEmbeddingModel(),
                        configDTO.getDimensions(),
                        JsonUtil.parseObject(configDTO.getConfig(), PgVectorSettings.class)
                ));
    }

    @Override
    public void close() {

    }
}

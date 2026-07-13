package com.aianik.anik.ai.vector.storage.vector.milvus;

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
public class MilvusVectorLifecycle implements Lifecycle {

    @Override
    public void start() {
        VectorStoreFactory.REGISTER.put(VectorStoreType.MILVUS, configDTO ->
                new MilvusAnikAiVectorStore(
                        configDTO.getEmbeddingModel(),
                        configDTO.getDimensions(),
                        JsonUtil.parseObject(configDTO.getConfig(), MilvusVectorSettings.class)
                ));
    }

    @Override
    public void close() {

    }
}

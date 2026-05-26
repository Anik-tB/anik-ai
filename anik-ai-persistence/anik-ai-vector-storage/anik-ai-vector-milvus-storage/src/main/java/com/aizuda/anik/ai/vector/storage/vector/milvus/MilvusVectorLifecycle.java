package com.aizuda.anik.ai.vector.storage.vector.milvus;

import com.aizuda.anik.ai.common.Lifecycle;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.vector.storage.enums.VectorStoreType;
import com.aizuda.anik.ai.vector.storage.vector.VectorStoreFactory;
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

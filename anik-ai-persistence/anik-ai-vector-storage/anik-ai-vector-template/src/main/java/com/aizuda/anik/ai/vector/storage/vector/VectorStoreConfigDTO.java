package com.aizuda.anik.ai.vector.storage.vector;

import com.aizuda.anik.ai.model.model.embedding.AnikEmbeddingModel;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2026-04-02
 */
@Data
@Builder
public class VectorStoreConfigDTO {

    private String config;
    private AnikEmbeddingModel embeddingModel;
    private Integer dimensions;

}

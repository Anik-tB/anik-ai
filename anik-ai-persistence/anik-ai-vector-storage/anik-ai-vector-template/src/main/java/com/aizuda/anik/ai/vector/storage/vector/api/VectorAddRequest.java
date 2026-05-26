package com.aizuda.anik.ai.vector.storage.vector.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Vector write requests (explicitly specify the index/collection name to avoid deducing it from metadata only)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorAddRequest {

    /**
     * Index or collection name (required).
     * RAG: usually {@code {indexPrefix}_{ragId}};
     * Memory: {@code memory_agent_{agentId}}.
     */
    private String indexName;

    private List<VectorDocument> documents;
}

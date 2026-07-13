package com.aianik.anik.ai.model.model.rerank;

import com.aianik.anik.ai.common.model.ModelCallException;
import com.aianik.anik.ai.model.model.Model;

import java.util.List;

/**
 * rearrange model interface
 */
public interface RerankModel extends Model {

    /**
     * Reorder candidate documents
     */
    RerankResponse rerank(RerankDTO dto) throws ModelCallException;

    /**
     * Reorder request DTO
     *
     * @param query query text
     * @param documents candidate document content list
     * @param topN returns the top N results
     */
    record RerankDTO(String query, List<String> documents, Integer topN) {
    }
}

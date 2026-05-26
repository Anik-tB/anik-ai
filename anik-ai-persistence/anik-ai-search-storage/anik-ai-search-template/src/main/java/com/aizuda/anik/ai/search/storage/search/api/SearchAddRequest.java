package com.aizuda.anik.ai.search.storage.search.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search document write requests (specify indexName explicitly to avoid implicit derivation from metadata).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchAddRequest {

    private String indexName;

    private List<SearchDocument> documents;
}

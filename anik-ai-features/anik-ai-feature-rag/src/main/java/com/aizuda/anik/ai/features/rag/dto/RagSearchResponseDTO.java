package com.aizuda.anik.ai.features.rag.dto;

import com.aizuda.anik.ai.common.dto.rag.SearchResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RagSearchResponseDTO {

    private List<SearchResult> results;

    private RagStageMetricsDTO metrics;
}

package com.aizuda.anik.ai.features.rag.search.pipeline;

import com.aizuda.anik.ai.common.dto.rag.SearchResult;
import com.aizuda.anik.ai.features.rag.dto.RagStageMetricsDTO;
import com.aizuda.anik.ai.persistence.rag.dataobject.RagConfigDO;
import com.aizuda.anik.ai.persistence.rag.po.RagPO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagSearchContext {

    // ── Enter ──
    private String originalQuery;
    private Long ragId;
    private boolean debug;

    // ── Configuration (ConfigResolveHandler populated) ──
    private RagPO knowledge;
    private RagConfigDO.SearchParams searchParams;
    private RagConfigDO.ModelParams modelParams;

    // ── Transfer data ──
    private String query;
    private List<SearchResult> vectorResults;
    private List<SearchResult> bm25Results;
    private List<SearchResult> results;

    // ──Indicators──
    private RagStageMetricsDTO metrics;

    // ──Control──
    private boolean terminated;

    // ── External incoming configuration (optional) ──
    private RagConfigDO externalConfigDO;
}

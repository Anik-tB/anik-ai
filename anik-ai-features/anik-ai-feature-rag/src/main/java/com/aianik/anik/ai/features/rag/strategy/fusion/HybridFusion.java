package com.aianik.anik.ai.features.rag.strategy.fusion;

import com.aianik.anik.ai.common.dto.rag.SearchResult;

import java.util.List;

public interface HybridFusion {

    List<SearchResult> fuse(List<SearchResult> vectorResults,
                            List<SearchResult> bm25Results,
                            int outputSize);
}

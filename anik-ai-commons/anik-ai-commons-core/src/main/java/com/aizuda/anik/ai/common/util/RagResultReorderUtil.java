package com.aizuda.anik.ai.common.util;

import com.aizuda.anik.ai.common.dto.rag.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG search results reordering tool
 * <p>
 * Lost in the Middle strategy: Distribute high-correlation results at the beginning and end, and place low-correlation results in the middle.
 * Mitigates LLM attention degradation for middle sections of long contexts.
 */
public final class RagResultReorderUtil {

    private RagResultReorderUtil() {
    }

    public static List<SearchResult> reorderForLostInTheMiddle(List<SearchResult> ranked) {
        if (ranked == null || ranked.size() <= 2) {
            return ranked;
        }
        List<SearchResult> reordered = new ArrayList<>(ranked.size());
        List<SearchResult> tail = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            if (i % 2 == 0) {
                reordered.add(ranked.get(i));
            } else {
                tail.add(ranked.get(i));
            }
        }
        for (int i = tail.size() - 1; i >= 0; i--) {
            reordered.add(tail.get(i));
        }
        return reordered;
    }
}

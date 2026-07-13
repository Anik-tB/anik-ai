package com.aianik.anik.ai.features.rag.dedup;

import com.aianik.anik.ai.persistence.rag.po.RagDocumentPO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Deduplication check results: whether it is hit, hit dimensions, conflicting old documents
 *
 * @author openanik
 */
@Getter
@AllArgsConstructor
public class DedupResult {

    private final DedupMatchType matchType;

    private final RagDocumentPO conflict;

    public static DedupResult none() {
        return new DedupResult(DedupMatchType.NONE, null);
    }

    public static DedupResult of(DedupMatchType type, RagDocumentPO conflict) {
        return new DedupResult(type, conflict);
    }

    public boolean isHit() {
        return matchType != DedupMatchType.NONE && conflict != null;
    }
}

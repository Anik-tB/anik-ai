package com.aizuda.anik.ai.features.rag.dedup;

import com.aizuda.anik.ai.persistence.rag.po.RagDocumentPO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Upload decision making of a single file: decision making type + hit dimension + conflict document
 *
 * @author openanik
 */
@Getter
@AllArgsConstructor
public class UploadDecision {

    private final UploadDecisionType type;

    private final DedupMatchType matchType;

    private final RagDocumentPO conflict;

    public static UploadDecision newFile() {
        return new UploadDecision(UploadDecisionType.NEW, DedupMatchType.NONE, null);
    }

    public static UploadDecision reject(DedupResult r) {
        return new UploadDecision(UploadDecisionType.REJECT, r.getMatchType(), r.getConflict());
    }

    public static UploadDecision skip(DedupResult r) {
        return new UploadDecision(UploadDecisionType.SKIP, r.getMatchType(), r.getConflict());
    }

    public static UploadDecision overwrite(DedupResult r) {
        return new UploadDecision(UploadDecisionType.OVERWRITE, r.getMatchType(), r.getConflict());
    }

    public boolean isNew() {
        return type == UploadDecisionType.NEW;
    }
}

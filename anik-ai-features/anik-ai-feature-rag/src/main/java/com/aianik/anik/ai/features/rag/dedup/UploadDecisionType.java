package com.aianik.anik.ai.features.rag.dedup;

/**
 * Upload decision making type
 *
 * @author openanik
 */
public enum UploadDecisionType {

    /** New files are stored normally */
    NEW,

    /** Duplicate hits and policy is deny */
    REJECT,

    /** Hits are repeated and the strategy is skip*/
    SKIP,

    /** The hit is repeated and the policy is coverage */
    OVERWRITE
}

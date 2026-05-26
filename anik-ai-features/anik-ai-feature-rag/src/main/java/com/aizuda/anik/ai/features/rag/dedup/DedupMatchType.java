package com.aizuda.anik.ai.features.rag.dedup;

/**
 * Deduplication hit dimension: used to explain to the user/front-end why this upload was identified as a duplicate
 *
 * @author openanik
 */
public enum DedupMatchType {

    /** Undetermined */
    NONE,

    /** file name hit*/
    BY_NAME,

    /**File content hit*/
    BY_CONTENT,

    /** Both file name and content are hit*/
    BOTH
}

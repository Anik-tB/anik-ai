package com.aizuda.anik.ai.common.enums.memory;

/**
 * memory compression strategy
 */
public enum CompressionStrategy {

    /** Sliding window: keeps the most recent N entries, compresses earlier ones into a summary and archives them */
    SLIDING_WINDOW,

    /** Importance-based filtering (reserved for future use) */
    IMPORTANCE_BASED;

    public static CompressionStrategy fromConfig(Integer raw) {
        if (raw == null) {
            return SLIDING_WINDOW;
        }
        return raw == CompressionStrategyEnum.IMPORTANCE_BASED.getStrategy()
                ? IMPORTANCE_BASED
                : SLIDING_WINDOW;
    }
}

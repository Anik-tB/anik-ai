package com.aianik.anik.ai.common.constants;

import java.math.BigDecimal;

/**
 * The default configuration shared by the memory module and vector retrieval (to prevent magic numbers from being scattered across the layers).
 */
public final class MemoryIntegrationDefaults {

    private MemoryIntegrationDefaults() {
    }

    /** The default model ID when embedding a model is not explicitly specified (consistent with the default model record in the library) */
    public static final long DEFAULT_EMBEDDING_MODEL_ID = 1L;

    /**
     * The number of days back when "recent memory" is pulled by Agent and is not filtered by type (about 10 years, equivalent to "all recent memories").
     */
    public static final int RECENT_MEMORIES_LOOKBACK_DAYS = 3650;

    /** Default relevance score when creating a new memory entry */
    public static final BigDecimal DEFAULT_RELEVANCE_SCORE = BigDecimal.valueOf(0.8);

    /**Default Confidence score during New memory*/
    public static final BigDecimal DEFAULT_CONFIDENCE_SCORE = BigDecimal.valueOf(0.8);
}

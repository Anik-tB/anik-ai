package com.aianik.anik.ai.route;

/**
 * Routing policy type constants
 *
 * @author openanik
 * @date 2025-04-08
 */
public final class RouteStrategyType {

    /** Least load routing*/
    public static final String LEAST_LOAD = "LEAST_LOAD";

    /** Random routing */
    public static final String RANDOM = "RANDOM";

    /** Polling routing*/
    public static final String ROUND_ROBIN = "ROUND_ROBIN";

    /** Consistent hash routing */
    public static final String CONSISTENT_HASH = "CONSISTENT_HASH";

    /** LRU least recently used route*/
    public static final String LRU = "LRU";

    /** Fixed routing (always choose the first one)*/
    public static final String FIRST = "FIRST";

    private RouteStrategyType() {
        throw new UnsupportedOperationException("Utility class");
    }
}

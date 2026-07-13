package com.aianik.anik.ai.route;

import com.aianik.anik.ai.ClientInstanceManager.ClientInstanceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Polling routing policy
 *
 * @author openanik
 * @date 2025-04-08
 */
@Component
public class RoundRobinClientRoute implements ClientRouteStrategy {

    /** Counter initial value */
    private static final long COUNTER_INITIAL_VALUE = 0L;

    private final AtomicLong counter = new AtomicLong(COUNTER_INITIAL_VALUE);

    @Override
    public ClientInstanceInfo select(List<ClientInstanceInfo> candidates, String routeKey) {
        int index = calculateIndex(candidates.size());
        return candidates.get(index);
    }

    @Override
    public String getType() {
        return RouteStrategyType.ROUND_ROBIN;
    }

    /**
     * Calculate polling index
     * @param candidateSize number of candidate instances
     * @return index value
     */
    private int calculateIndex(int candidateSize) {
        long currentCount = counter.getAndIncrement();
        int index = (int) (currentCount % candidateSize);
        return Math.abs(index);
    }
}

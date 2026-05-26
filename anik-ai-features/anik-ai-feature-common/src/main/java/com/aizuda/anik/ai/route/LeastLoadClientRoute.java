package com.aizuda.anik.ai.route;

import com.aizuda.anik.ai.ClientInstanceManager.ClientInstanceInfo;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Least load routing (default policy)
 *
 * @author openanik
 * @date 2025-04-08
 */
@Component
public class LeastLoadClientRoute implements ClientRouteStrategy {

    /** Zero concurrency threshold */
    private static final int ZERO_CONCURRENT_THRESHOLD = 0;

    @Override
    public ClientInstanceInfo select(List<ClientInstanceInfo> candidates, String routeKey) {
        return candidates.stream()
                .min(Comparator.comparingDouble(this::calculateLoadRatio))
                .orElseGet(() -> candidates.isEmpty() ? null : candidates.get(0));
    }

    @Override
    public String getType() {
        return RouteStrategyType.LEAST_LOAD;
    }

    /**
     * Calculate load ratio
     * @param client client instance
     * @return load ratio, between 0-1 indicates normal load, Double.MAX_VALUE indicates invalid instance
     */
    private double calculateLoadRatio(ClientInstanceInfo client) {
        return client.getMaxConcurrent() > ZERO_CONCURRENT_THRESHOLD
                ? (double) client.getActiveChats() / client.getMaxConcurrent()
                : Double.MAX_VALUE;
    }
}

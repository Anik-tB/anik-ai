package com.aizuda.anik.ai.route;

import com.aizuda.anik.ai.ClientInstanceManager.ClientInstanceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random routing strategy
 *
 * @author openanik
 * @date 2025-04-08
 */
@Component
public class RandomClientRoute implements ClientRouteStrategy {

    @Override
    public ClientInstanceInfo select(List<ClientInstanceInfo> candidates, String routeKey) {
        int randomIndex = generateRandomIndex(candidates.size());
        return candidates.get(randomIndex);
    }

    @Override
    public String getType() {
        return RouteStrategyType.RANDOM;
    }

    /**
     * Generate random index
     * @param candidateSize number of candidate instances
     * @return random index
     */
    private int generateRandomIndex(int candidateSize) {
        return ThreadLocalRandom.current().nextInt(candidateSize);
    }
}

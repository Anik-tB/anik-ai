package com.aizuda.anik.ai.route;

import com.aizuda.anik.ai.ClientInstanceManager.ClientInstanceInfo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fixed routing strategy (always select the first instance, suitable for single client scenarios)
 *
 * @author openanik
 * @date 2025-04-09
 */
@Component
public class FirstClientRoute implements ClientRouteStrategy {

    @Override
    public ClientInstanceInfo select(List<ClientInstanceInfo> candidates, String routeKey) {
        return candidates.get(0);
    }

    @Override
    public String getType() {
        return RouteStrategyType.FIRST;
    }
}

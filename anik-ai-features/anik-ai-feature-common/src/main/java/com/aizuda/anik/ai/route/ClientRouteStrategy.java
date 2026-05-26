package com.aizuda.anik.ai.route;

import com.aizuda.anik.ai.ClientInstanceManager.ClientInstanceInfo;

import java.util.List;

/**
 * client routing policy interface
 */
public interface ClientRouteStrategy {

    /**
     * Select one from candidate instances
     *
     * @param Client instance list of candidates active
     * @param routeKey routing key (such as conversationId, used for consistent hashing)
     * @return selected instance
     */
    ClientInstanceInfo select(List<ClientInstanceInfo> candidates, String routeKey);

    String getType();
}

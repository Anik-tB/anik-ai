package com.aizuda.anik.ai.route;

import com.aizuda.anik.ai.ClientInstanceManager.ClientInstanceInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Consistent hash routing (conversation affinity based on conversationId)
 *
 * @author openanik
 * @date 2025-04-08
 */
@Component
public class ConsistentHashClientRoute implements ClientRouteStrategy {

    /** Number of virtual nodes */
    private static final int VIRTUAL_NODES_COUNT = 100;
    
    /** Hash seed */
    private static final int HASH_SEED = 31;
    
    /** Hash mask (guaranteed to be non-negative) */
    private static final long HASH_MASK = 0x7fffffffffffffffL;
    
    /** Virtual node separator */
    private static final String VIRTUAL_NODE_SEPARATOR = "#";

    @Override
    public ClientInstanceInfo select(List<ClientInstanceInfo> candidates, String routeKey) {
        TreeMap<Long, ClientInstanceInfo> hashRing = buildHashRing(candidates);
        return selectFromRing(hashRing, routeKey);
    }

    @Override
    public String getType() {
        return RouteStrategyType.CONSISTENT_HASH;
    }

    /**
     * Build a hash ring
     */
    private TreeMap<Long, ClientInstanceInfo> buildHashRing(List<ClientInstanceInfo> candidates) {
        TreeMap<Long, ClientInstanceInfo> ring = new TreeMap<>();
        
        for (ClientInstanceInfo client : candidates) {
            addVirtualNodes(ring, client);
        }
        
        return ring;
    }

    /**
     * Add virtual node
     */
    private void addVirtualNodes(TreeMap<Long, ClientInstanceInfo> ring, ClientInstanceInfo client) {
        for (int i = 0; i < VIRTUAL_NODES_COUNT; i++) {
            String virtualNodeKey = buildVirtualNodeKey(client.getHostId(), i);
            long hash = calculateHash(virtualNodeKey);
            ring.put(hash, client);
        }
    }

    /**
     * Build virtual node keys
     */
    private String buildVirtualNodeKey(String hostId, int index) {
        return hostId + VIRTUAL_NODE_SEPARATOR + index;
    }

    /**
     * Select nodes from hash ring
     */
    private ClientInstanceInfo selectFromRing(TreeMap<Long, ClientInstanceInfo> ring, String routeKey) {
        long keyHash = calculateHash(routeKey);
        SortedMap<Long, ClientInstanceInfo> tailMap = ring.tailMap(keyHash);
        
        return tailMap.isEmpty() 
                ? ring.firstEntry().getValue() 
                : tailMap.get(tailMap.firstKey());
    }

    /**
     * Calculate hash value
     */
    private long calculateHash(String key) {
        long hash = 0;
        for (char c : key.toCharArray()) {
            hash = HASH_SEED * hash + c;
        }
        return hash & HASH_MASK;
    }
}

package com.aianik.anik.ai.memory.store;

import com.aianik.anik.ai.memory.dto.ShortTermHistoryQuery;
import com.aianik.anik.ai.memory.dto.ShortTermMessage;

import java.util.List;

/**
 * Short-term memory storage policy interface
 *
 * Supports multiple storage media (Redis, JVM memory, etc.) through policy mode,
 * This is achieved by configuring the anik.ai.memory.short-term.store-type switch.
 *
 * author: openanik
 * date: 2026-03-26
 */
public interface ShortTermMemoryStore {

    /**
     * Append a message to short-term memory (write path)
     *
     * @param conversationId Session ID
     * @param role message role (user/assistant)
     * @param content message content
     * @param windowSize The maximum number of items retained by the sliding window (>=1)
     */
    void append(String conversationId, String role, String content, int windowSize);

    /**
     * Load historical messages (read path)
     *
     * Return the time sequence list, excluding the last one (the current User messages are already in Prompt to avoid duplication).
     * If there is no data in the storage (cold start/restart), the implementation class should fall back to the DB by itself and backfill it.
     *
     * @param query query parameter (including agentId, userId, conversationId)
     * @param windowSize sliding window and DB fall back to LIMIT (>=1)
     * @return Historical message list (excluding the latest one)
     */
    List<ShortTermMessage> loadHistory(ShortTermHistoryQuery query, int windowSize);

    /**
     * Proactively clear short-term memory for a specified session (can be called at the end of the session)
     *
     * @param conversationId Session ID
     */
    void evict(String conversationId);

    /**
     * Storage type identifier, corresponding to the configuration item store-type
     *
     * @return type identifier, such as "redis", "memory"
     */
    String storeType();
}

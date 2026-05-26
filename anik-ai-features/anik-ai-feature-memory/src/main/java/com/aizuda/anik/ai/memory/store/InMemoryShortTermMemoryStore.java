package com.aizuda.anik.ai.memory.store;

import com.aizuda.anik.ai.memory.dto.ShortTermHistoryQuery;
import com.aizuda.anik.ai.memory.dto.ShortTermMessage;
import com.aizuda.anik.ai.persistence.agent.mapper.AgentConversationRecordMapper;
import com.aizuda.anik.ai.persistence.agent.po.AgentConversationRecordPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Short-term memory JVM memory storage implementation
 *
 * Suitable for stand-alone deployment or scenarios that do not rely on Redis.
 * Use ConcurrentHashMap to store the bounded message queue (LinkedList) of each session, supporting sliding window.
 *
 * Note: When deploying multiple instances, each node has independent memory, and data will be lost after restarting (it will automatically fall back from the DB).
 *
 * Activation mode: anik.ai.memory.short-term.store-type: memory
 *
 * author: openanik
 * date: 2026-03-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "anik-ai.memory.short-term.store-type", havingValue = "memory")
public class InMemoryShortTermMemoryStore implements ShortTermMemoryStore {

    private final AgentConversationRecordMapper recordMapper;

    /** conversationId -> bounded message queue (time sequence) */
    private final ConcurrentHashMap<String, LinkedList<ShortTermMessage>> store = new ConcurrentHashMap<>();

    @Override
    public void append(String conversationId, String role, String content, int windowSize) {
        int cap = Math.max(1, windowSize);
        store.compute(conversationId, (k, list) -> {
            if (list == null) list = new LinkedList<>();
            list.addLast(new ShortTermMessage(role, content));
            //Maintain sliding window
            while (list.size() > cap) {
                list.removeFirst();
            }
            return list;
        });
    }

    @Override
    public List<ShortTermMessage> loadHistory(ShortTermHistoryQuery query, int windowSize) {
        int cap = Math.max(1, windowSize);
        LinkedList<ShortTermMessage> list = store.get(query.getConversationId());

        List<ShortTermMessage> all;
        if (list == null || list.isEmpty()) {
            // No data in memory: fall back from DB and backfill
            all = loadFromDbAndFill(query, cap);
        } else {
            all = new ArrayList<>(list);
        }

        //Exclude the last one (current User messages, already in Prompt)
        if (all.size() <= 1) {
            return Collections.emptyList();
        }
        return all.subList(0, all.size() - 1);
    }

    @Override
    public void evict(String conversationId) {
        store.remove(conversationId);
    }

    @Override
    public String storeType() {
        return "memory";
    }

    // ==================== Private methods ====================

    private List<ShortTermMessage> loadFromDbAndFill(ShortTermHistoryQuery query, int windowSize) {
        List<AgentConversationRecordPO> records = recordMapper.selectList(
                new LambdaQueryWrapper<AgentConversationRecordPO>()
                        .eq(AgentConversationRecordPO::getAgentId, query.getAgentId())
                        .eq(AgentConversationRecordPO::getConversationId, query.getConversationId())
                        .eq(AgentConversationRecordPO::getUserId, query.getUserId())
                        .orderByAsc(AgentConversationRecordPO::getCreateDt)
                        .last("LIMIT " + windowSize));

        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShortTermMessage> messages = records.stream()
                .map(r -> new ShortTermMessage(r.getRole(), r.getContent()))
                .collect(Collectors.toList());

        // Backfill memory
        LinkedList<ShortTermMessage> linked = new LinkedList<>(messages);
        store.put(query.getConversationId(), linked);
        log.debug("Short-term memory cache miss, backfill {} items from DB, conversationId={}",
                messages.size(), query.getConversationId());

        return messages;
    }
}

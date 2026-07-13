package com.aianik.anik.ai.memory.store;

import com.aianik.anik.ai.memory.dto.ShortTermHistoryQuery;
import com.aianik.anik.ai.memory.dto.ShortTermMessage;
import com.aianik.anik.ai.persistence.agent.mapper.AgentConversationRecordMapper;
import com.aianik.anik.ai.persistence.agent.po.AgentConversationRecordPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Short-term memory database storage implementation
 *
 * Suitable for distributed deployment scenarios and store short-term memory directly into the database.
 * All short-term memory operations are persisted through the AgentConversationRecord table.
 *
 * advantage:
 * -Supports multi-instance deployment (shared database)
 * - Data will not be lost after the instance is restarted
 * - Facilitates auditing and historical tracking
 *
 * shortcoming:
 * - Lower performance than Redis/in-memory implementation (every operation is a DB call)
 * - Need to regularly clean up expired data
 *
 * Activation method: anik.ai.memory.short-term.store-type: db
 *
 * author: openanik
 * date: 2026-05-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "anik-ai.memory.short-term.store-type", matchIfMissing = true, havingValue = "db")
public class DbShortTermMemoryStore implements ShortTermMemoryStore {

    private final AgentConversationRecordMapper recordMapper;

    @Override
    public void append(String conversationId, String role, String content, int windowSize) {
        // Already written in ConversationRecordCallbackHandler and ConversationHandler
    }

    @Override
    public List<ShortTermMessage> loadHistory(ShortTermHistoryQuery query, int windowSize) {
        int cap = Math.max(1, windowSize);

        // Read the latest windowSize records from DB (excluding the last one)
        List<AgentConversationRecordPO> records = recordMapper.selectList(
                new LambdaQueryWrapper<AgentConversationRecordPO>()
                        .eq(AgentConversationRecordPO::getAgentId, query.getAgentId())
                        .eq(AgentConversationRecordPO::getConversationId, query.getConversationId())
                        .eq(AgentConversationRecordPO::getUserId, query.getUserId())
                        .orderByAsc(AgentConversationRecordPO::getCreateDt)
                        .last("LIMIT " + cap));

        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShortTermMessage> messages = records.stream()
                .map(r -> new ShortTermMessage(r.getRole(), r.getContent()))
                .collect(Collectors.toList());

        //Exclude the last one (current User messages, already in Prompt)
        if (messages.size() <= 1) {
            return Collections.emptyList();
        }

        return messages.subList(0, messages.size() - 1);
    }

    @Override
    public void evict(String conversationId) {
        //delete all short-term memory records for the specified session
        int deleted = recordMapper.delete(
                new LambdaQueryWrapper<AgentConversationRecordPO>()
                        .eq(AgentConversationRecordPO::getConversationId, conversationId));

        log.debug("Short-term memory DB cleanup: conversationId={}, deletedCount={}", 
                conversationId, deleted);
    }

    @Override
    public String storeType() {
        return "db";
    }
}

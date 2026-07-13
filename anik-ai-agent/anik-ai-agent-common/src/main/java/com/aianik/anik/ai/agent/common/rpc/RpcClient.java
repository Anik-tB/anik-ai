package com.aianik.anik.ai.agent.common.rpc;

import com.aianik.anik.ai.agent.common.rpc.annotation.Mapping;
import com.aianik.anik.ai.common.dto.agent.ConversationCreateRequest;
import com.aianik.anik.ai.common.dto.agent.ConversationRecordRequest;
import com.aianik.anik.ai.common.dto.agent.SkillContentRequest;
import com.aianik.anik.ai.common.dto.agent.SkillContentResponse;
import com.aianik.anik.ai.common.dto.memory.ShortTermMemoryRequest;
import com.aianik.anik.ai.common.dto.rag.RagSearchRequest;
import com.aianik.anik.ai.common.dto.rag.RagSearchResponse;
import com.aianik.anik.ai.common.grpc.constant.UriConstants;

import java.util.List;
import java.util.Map;



/**
 * Request Server interface
 *
 * @author openanik
 * @date 2025-04-12
 */
public interface RpcClient {
    
    /**
     * Create conversations and save User messages
     */
    @Mapping(path = UriConstants.CALLBACK_CONVERSATION_CREATE)
    void createConversation(ConversationCreateRequest request);
    
    /**
     * Save conversation message history
     */
    @Mapping(path = UriConstants.CALLBACK_CONVERSATION_RECORD)
    void saveRecord(ConversationRecordRequest request);

    /**
     * Load short-term conversation history
     */
    @Mapping(path = UriConstants.CALLBACK_MEMORY_SHORT_TERM)
    List<Map<String, Object>> loadShortTermHistory(ShortTermMemoryRequest request);

    /**
     * Get Skill full content
     */
    @Mapping(path = UriConstants.CALLBACK_SKILL_CONTENT)
    SkillContentResponse fetchSkillContent(SkillContentRequest request);

    /**
     * Knowledge base search
     */
    @Mapping(path = UriConstants.CALLBACK_RAG_SEARCH, timeout = 120_000)
    RagSearchResponse searchRag(RagSearchRequest request);


}

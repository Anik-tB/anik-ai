package com.aizuda.anik.ai.model.service;

/**
 * AI model uses statistical service interface (infrastructure layer)
 * Contains only methods required by the model infrastructure, implemented by the admin layer
 */
public interface AiModelUsageService {

    void recordUsage(Long modelId, Long userId, Integer promptTokens, Integer completionTokens,
                     Long responseTime, Integer status, String errorMessage, String conversationId);
}

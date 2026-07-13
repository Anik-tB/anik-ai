package com.aianik.anik.ai.persistence.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI model configuration persistence object
 * Table: anik_ai_model_config
 *
 * Supports flexible configuration of multiple providers and model types
 * Supports overall situation configuration (owner_id=null) and personal configuration (owner_id=userId)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("anik_ai_model_config")
public class AiModelConfigPO {

    /**
     * Model configuration ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Provider ID (foreign key)
     * Linked to anik_ai_model_provider.id
     */
    private Long providerId;

    /**
     * Model name
     * For example: gpt-4, claude-3-opus, llama2
     */
    private String modelName;

    /**
     * Model identifier (unique)
     * For example: gpt-4, claude-opus-3, llama2-13b
     */
    private String modelKey;

    /**
     * Model type
     * CHAT: dialogue model
     * EMBEDDING: vector model
     * RERANKER: rearrange model
     * IMAGE: image model
     * SPEECH: speech model
     */
    private String modelType;

    /**
     * Model description
     */
    private String description;

    /**
     * API key (encrypted storage)
     */
    private String apiKey;

    /**
     * API endpoint URL
     */
    private String apiEndpoint;

    /**
     * Model parameter configuration (JSONB format)
     * For example:
     * {
     *   "temperature": 0.7,
     *   "maxTokens": 2000,
     *   "topP": 0.9,
     *   "timeoutMs": 30000
     * }
     */
    private String configJson;

    /**
     * Owner ID
     * NULL: global model (Admin configuration)
     * Specific value: userID (personal configuration)
     */
    private Long ownerId;

    /**
     * Scope
     * GLOBAL: overall situation (Admin configuration)
     * PERSONAL: personal (user configuration)
     */
    private String scope;

    /**
     * Is this the default model for this model type?
     */
    private Boolean isDefault;

    /**
     * Whether enabled
     */
    private Boolean isEnabled;

    /**
     * creation time
     */
    private LocalDateTime createdDt;

    /**
     * Update time
     */
    private LocalDateTime updatedDt;
}

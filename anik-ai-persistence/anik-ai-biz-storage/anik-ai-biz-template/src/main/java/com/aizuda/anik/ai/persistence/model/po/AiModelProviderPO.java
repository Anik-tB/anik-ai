package com.aizuda.anik.ai.persistence.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI model provider information persistence object
 * Table: anik_ai_model_provider
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("anik_ai_model_provider")
public class AiModelProviderPO {

    /**
     * Provider ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Provider name (unique)
     * For example: OpenAI, Claude, Ollama
     */
    private String providerName;

    /**
     * Provider identifier (unique)
     * For example: openai, claude, ollama
     */
    private String providerKey;

    /**
     * Provider description
     */
    private String description;

    /**
     * LOGO icon URL
     */
    private String iconUrl;

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

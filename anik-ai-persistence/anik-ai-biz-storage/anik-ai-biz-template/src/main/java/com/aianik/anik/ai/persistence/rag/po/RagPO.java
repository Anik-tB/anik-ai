package com.aianik.anik.ai.persistence.rag.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RAG persistent object
 * Table: anik_ai_rag
 *
 * Represents a knowledge base
 * The knowledge base consists of multiple documents and vector library
 * Supports flexible configuration of multiple vector libraries and search engines
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_rag")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RagPO {

    /**
     * Knowledge base ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Knowledge base name
     * Display name for user identification
     */
    private String name;

    /**
     * Knowledge base description
     * Description of the functions and content of the knowledge base
     */
    private String description;

    /**
     * Knowledge Base Icon URL
     * Icons/thumbnails displayed in the UI
     */
    private String icon;

    /**
     * Vector store instance ID (foreign key, can be null)
     * Linked to anik_ai_store_instance.id
     * This ID can be used to query the type information of the vector library.
     */
    private Long vectorStoreInstanceId;

    /**
     * vector dimensions (optional)
     * Use embedding model default dimensions when empty
     */
    private Integer dimensionOfVectorModel;

    /**
     * Embed model ID (foreign key)
     * Related to anik_ai_model_config.id
     * Embedding model for converting text into vectors
     */
    private Long embeddingModelId;

    /**
     * rearrange modelID (foreign key, can be null)
     * Related to anik_ai_model_config.id
     * Used to rearrange search results
     */
    private Long rerankModelId;

    /**
     * Whether enabledsearch engine
     * true: enablesearch engine enhanced retrieval
     * false: only use vector retrieval
     */
    private Boolean searchEngineEnable;

    /**
     * search engine instance ID (foreign key, can be null)
     * Linked to anik_ai_store_instance.id
     * This ID can be used to associate and query the type information of the search engine.
     */
    private Long searchEngineInstanceId;

    /**
     * separator
     * Delimiter rules for text splitting
     * For example: periods, newlines, specific characters, etc.
     */
    private String delimiter;

    /**
     * RAG enhanced configuration
     * Enhancement strategies for improving retrieval performance
     */
    private String ragEnhancement;

    /**
     * Configuration parameters (JSON format)
     * Extended configuration for storing knowledge base
     */
    private String config;

    /**
     * Document deduplication strategy
     * 0=NONE, 1=BY_NAME, 2=BY_CONTENT, 3=BY_NAME_OR_CONTENT
     */
    private Integer dedupStrategy;

    /**
     * Processing actions during hit deduplication
     * 0=REJECT, 1=SKIP, 2=OVERWRITE
     */
    private Integer dedupAction;

    /**
     * Whether to perform a second confirmation before uploading
     * true: go through preview → commit two stages; false: direct transfer
     */
    private Boolean uploadConfirm;

    /**
     * creation time
     * The moment when the knowledge base is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time the knowledge base was refreshed
     */
    private LocalDateTime updateDt;
}

package com.aizuda.anik.ai.admin.vo.knowledge;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeRequestVO {

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    private String icon;

    /** Vector store instance ID (VECTOR_STORE) */
    private Long vectorStoreInstanceId;

    /** Vector dimension (required), must not exceed the max supported by the model or vector store */
    @NotNull(message = "dimensionOfVectorModel is required")
    private Integer dimensionOfVectorModel;

    @NotNull(message = "embeddingModelId is required")
    private Long embeddingModelId;

    private Long rerankModelId;

    private Boolean searchEngineEnable;

    /** Search engine instance ID (SEARCH_ENGINE), used when hybrid retrieval is enabled */
    private Long searchEngineInstanceId;

    private String delimiter;

    @JsonAlias("knowledgeEnhancement")
    private String ragEnhancement;

    // ---------- Slicing strategy (write config.chunkParams, and synchronize delimiter field) ----------

    /** default | delimiter */
    private String chunkMode;

    private Integer maxChunkTokens;

    private Integer chunkOverlap;

    /** First-level delimiter when chunkMode=delimiter */
    private String customDelimiter;

    /** First-level segmentation regularity when chunkMode=regex (Java Pattern syntax) */
    private String chunkRegex;

    /** Chat model ID for intelligent chunking when chunkMode=smart (model config) */
    private Long chunkModelId;

    private Boolean mergeShortSegments;

    private Boolean imageOcr;

    // ---------- Upload deduplication strategy ----------

    /** Deduplication strategy: 0=NONE 1=BY_NAME 2=BY_CONTENT 3=BY_NAME_OR_CONTENT */
    private Integer dedupStrategy;

    /** Conflicting actions: 0=REJECT 1=SKIP 2=OVERWRITE */
    private Integer dedupAction;

    /** Whether confirmation is required before uploading */
    private Boolean uploadConfirm;
}

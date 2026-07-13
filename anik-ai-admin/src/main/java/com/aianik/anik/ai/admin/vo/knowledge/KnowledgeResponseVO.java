package com.aianik.anik.ai.admin.vo.knowledge;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KnowledgeResponseVO {

    private Long id;

    private String name;

    private String description;

    private String icon;

    private Long vectorStoreInstanceId;

    /** Vector dimensions (use model default dimensions when empty) */
    private Integer dimensionOfVectorModel;

    private Long embeddingModelId;

    /** Embedding model display name (resolved from embeddingModelId, response-only) */
    private String embeddingModelName;

    private Long rerankModelId;

    private Boolean searchEngineEnable;

    private Long searchEngineInstanceId;

    private String delimiter;

    @JsonAlias("knowledgeEnhancement")
    private String ragEnhancement;

    private KnowledgeConfigRequestVO config;

    private Integer documentCount;

    private Integer chunkCount;

    /** Deduplication strategy: 0=NONE 1=BY_NAME 2=BY_CONTENT 3=BY_NAME_OR_CONTENT */
    private Integer dedupStrategy;

    /** Conflicting actions: 0=REJECT 1=SKIP 2=OVERWRITE */
    private Integer dedupAction;

    /** Whether confirmation is required before uploading */
    private Boolean uploadConfirm;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;
}

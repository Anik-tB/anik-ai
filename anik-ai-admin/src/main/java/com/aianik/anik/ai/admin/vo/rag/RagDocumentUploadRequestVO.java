package com.aianik.anik.ai.admin.vo.rag;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RagDocumentUploadRequestVO {

    @JsonAlias("knowledgeId")
    @NotNull(message = "ragId is required")
    private Long ragId;

    private String name;

    /**
     * UPLOAD | URL | TEXT
     */
    private String sourceType;

    /**
     * Required when sourceType = URL
     */
    private String url;

    /**
     * Required when sourceType = TEXT
     */
    private String content;

    /**
     * Single overwrite RAG default deduplication strategy: 0=NONE 1=BY_NAME 2=BY_CONTENT 3=BY_NAME_OR_CONTENT
     * If empty, use RAG configuration
     */
    private Integer dedupStrategy;

    /**
     * Single overwrite RAG default conflict action: 0=REJECT 1=SKIP 2=OVERWRITE
     * If empty, use RAG configuration
     */
    private Integer dedupAction;
}

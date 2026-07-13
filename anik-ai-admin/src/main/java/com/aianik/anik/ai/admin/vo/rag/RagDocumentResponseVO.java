package com.aianik.anik.ai.admin.vo.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RagDocumentResponseVO {

    private Long id;

    private Long ragId;

    private String name;

    private String fileType;

    private String sourceType;

    private Integer status;

    private String errorMsg;

    private Integer chunkCount;

    private Long fileSize;

    private Long resourceId;

    /**
     * Decision result: NEW / SKIP / OVERWRITE
     * REJECT is returned via exception and will not appear in this field
     */
    private String decision;

    /** Matched conflict dimension: NONE / BY_NAME / BY_CONTENT / BOTH */
    private String matchType;

    /** Conflicting old document ID, only returned with SKIP/OVERWRITE */
    private Long conflictDocumentId;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;
}

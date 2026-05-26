package com.aizuda.anik.ai.admin.service.rag.preview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Preview status: stored in Redis, parsed and played back during commit
 *
 * @author openanik
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadPreviewState {

    private Long ragId;
    private Long userId;
    private Integer dedupStrategy;
    private Integer dedupAction;
    private List<UploadPreviewItemState> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UploadPreviewItemState {
        private Long tempResourceId;
        private String fileName;
        private String fileType;
        private String sourceType;
        private Long fileSize;
        private String contentHash;
        /** Initial decision making in the preview phase*/
        private String decision;
        /** Hit dimension*/
        private String matchType;
        /** Conflicting old document ID */
        private Long conflictDocumentId;
    }
}

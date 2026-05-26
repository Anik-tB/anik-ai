package com.aizuda.anik.ai.admin.vo.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Upload preview: single file prediction results
 *
 * @author openanik
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadPreviewItemVO {

    /** Temporary resource ID (reused during commit) */
    private Long tempResourceId;

    /** file name */
    private String fileName;

    /** File type */
    private String fileType;

    /** File size (bytes) */
    private Long fileSize;

    /** SHA-256 content hash */
    private String contentHash;

    /** Decision type: NEW / REJECT / SKIP / OVERWRITE */
    private String decision;

    /** Match dimension: NONE / BY_NAME / BY_CONTENT / BOTH */
    private String matchType;

    /** Conflicting old document ID */
    private Long conflictDocumentId;

    /** Conflicting old document name (for front-end display) */
    private String conflictDocumentName;

    /** Rejection reason (returned only when REJECT) */
    private String rejectReason;
}

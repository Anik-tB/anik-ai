package com.aizuda.anik.ai.admin.vo.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Upload preview returns: token + prediction results of each file
 *
 * @author openanik
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadPreviewResultVO {

    /** Preview token, returned when commit/cancel */
    private String previewToken;

    /** RAG ID */
    private Long ragId;

    /** Prediction results of each file (the order is consistent with the upload order) */
    private List<UploadPreviewItemVO> items;
}

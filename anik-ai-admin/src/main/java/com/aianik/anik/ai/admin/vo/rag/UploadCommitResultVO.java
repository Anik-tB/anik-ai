package com.aianik.anik.ai.admin.vo.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Commit result: the final landing status of each file (including TOCTOU conflict alarm)
 *
 * @author openanik
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadCommitResultVO {

    /** Whether there is a conflicting change (TOCTOU), the front end needs to confirm again */
    private Boolean conflictChanged;

    /** Implementation results of each document */
    private List<RagDocumentResponseVO> items;
}

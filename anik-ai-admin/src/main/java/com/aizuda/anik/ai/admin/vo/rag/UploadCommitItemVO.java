package com.aizuda.anik.ai.admin.vo.rag;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Final per-file decision at commit time (frontend can override row by row)
 *
 * @author openanik
 */
@Data
public class UploadCommitItemVO {

    @NotNull(message = "tempResourceId is required")
    private Long tempResourceId;

    /** Final user-selected decision: NEW / SKIP / OVERWRITE (REJECT cannot be submitted via commit) */
    @NotNull(message = "decision is required")
    private String decision;
}

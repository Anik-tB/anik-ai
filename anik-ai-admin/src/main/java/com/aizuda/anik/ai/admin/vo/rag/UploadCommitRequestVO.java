package com.aizuda.anik.ai.admin.vo.rag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Commit request: submit final decision per record using previewToken + user choice
 *
 * @author openanik
 */
@Data
public class UploadCommitRequestVO {

    @NotBlank(message = "previewToken is required")
    private String previewToken;

    @NotEmpty(message = "items is required")
    @Valid
    private List<UploadCommitItemVO> items;
}

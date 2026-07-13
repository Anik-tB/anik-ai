package com.aianik.anik.ai.admin.vo.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2025-07-12
 */
@Data
public class TxtEmbeddingRequestVO {
    private String fileName;
    @NotBlank
    private String filePath;
}

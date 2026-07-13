package com.aianik.anik.ai.admin.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2025-07-13
 */
@Data
public class AuthorizeRequestVO {
    @NotBlank
    private String email;
    private Integer days;
    private Integer totals;
}

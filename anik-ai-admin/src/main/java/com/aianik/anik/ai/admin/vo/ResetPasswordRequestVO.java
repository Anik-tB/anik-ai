package com.aianik.anik.ai.admin.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Reset password request VO
 *
 * @author openanik
 * @date 2025-04-27
 */
@Data
public class ResetPasswordRequestVO {
    /**
     * New Password
     */
    @NotBlank(message = "Password cannot be empty")
    private String password;
}

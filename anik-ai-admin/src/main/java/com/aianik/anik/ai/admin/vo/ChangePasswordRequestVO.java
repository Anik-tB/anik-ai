package com.aianik.anik.ai.admin.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * User self-service password change request VO
 *
 * @author openanik
 * @date 2026-04-28
 */
@Data
public class ChangePasswordRequestVO {

    @NotBlank(message = "The old password cannot be empty")
    private String oldPassword;

    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, max = 18, message = "The password length must be between 6-18 characters")
    private String newPassword;
}

package com.aizuda.anik.ai.admin.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request VO
 *
 * @author openanik
 * @date 2025-07-12
 */
@Data
public class LoginRequestVO {
    @NotBlank(message = "Account cannot be empty")
    private String username;
    
    @NotBlank(message = "Password cannot be empty")
    private String password;
}

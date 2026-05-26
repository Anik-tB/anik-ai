package com.aizuda.anik.ai.admin.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Update user request VO
 *
 * @author openanik
 * @date 2025-04-28
 */
@Data
public class UserUpdateRequestVO {
    /**
     * Role (1=Ordinary user, 2=Administrator)
     */
    @NotNull(message = "Role cannot be empty")
    private Integer role;
    
    /**
     * Email (optional)
     */
    @Email(message = "Email format is incorrect")
    private String email;
    
    /**
     * Password (optional, leave blank to keep existing password)
     */
    private String password;
}

package com.aianik.anik.ai.admin.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Create user request VO
 *
 * @author openanik
 * @date 2025-04-27
 */
@Data
public class UserCreateRequestVO {
    /**
     * Username (login account)
     */
    @NotBlank(message = "Username cannot be empty")
    private String username;
    
    /**
     * Mail
     */
    @Email(message = "Email format is incorrect")
    private String email;
    
    /**
     * password
     */
    @NotBlank(message = "Password cannot be empty")
    private String password;
    
    /**
     * Role (1=Ordinary user, 2=Administrator)
     */
    @NotNull(message = "Role cannot be empty")
    private Integer role;

}

package com.aianik.anik.ai.persistence.admin.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User info persistent object
 * Table: anik_ai_user
 *
 * @author openanik
 * @date 2026-04-14
 */
@Data
@TableName("anik_ai_user")
public class UserPO {

    /**
     * userID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * user role
     * 0: Ordinary user
     * 1: Advanced user
     * 2: administrator
     * 3: Super administrator
     */
    private Integer role;

    /**
     * user name
     * Unique username to use when logging in
     */
    private String username;

    /**
     * Email
     * user's email address
     * Used for password resets, notifications, etc.
     */
    private String email;

    /**
     * Password (encrypted storage)
     * Hash value of user login password
     * Encrypt using bcrypt or similar algorithm
     */
    private String password;

    /**
     * creation time
     * The moment when the user account is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time User info was renewed
     */
    private LocalDateTime updateDt;
}

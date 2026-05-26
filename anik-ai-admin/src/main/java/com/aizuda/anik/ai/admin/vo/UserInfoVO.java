package com.aizuda.anik.ai.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * author: openanik
 * date: 2025-07-18
 */
@Data
public class UserInfoVO {

    private Long id;

    private Integer role;

    /** Role display name */
    private String roleName;

    /** Login name */
    private String username;

    private String email;

    private Long tokens;

    private Integer totals;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireDt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createDt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateDt;

}

package com.aianik.anik.ai.persistence.app.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * App application information persistence object
 * Table: anik_ai_app
 *
 * Used to manage and isolate configuration, data, Agent and other resources of different applications
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_app")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppPO {

    /**
     * Application ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Application unique identifier
     * Used to identify the application when making API calls, usually a UUID or a custom unique string
     */
    private String appId;

    /**
     * Application name
     * Application display name for administrator or user identification
     */
    private String appName;

    /**
     * Application description
     * Detailed description of app functionality and usage
     */
    private String description;

    /**
     * Application access token (encrypted storage)
     * Authentication for API requests
     */
    private String token;

    /**
     * Routing strategy
     * Define how the application’s Agent, data, model and other resources are distributed and accessed
     * Possible values: LOCAL (local), CLUSTER (cluster), HYBRID (hybrid), etc.
     */
    private String routeStrategy;

    /**
     * Application status
     * 0: Disable/disable
     * 1: enable/active
     */
    private Integer status;

    /**
     * creation time
     * Record the moment the app was first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * Record the last time the application was refreshed
     */
    private LocalDateTime updateDt;
}

package com.aizuda.anik.ai.persistence.resource.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Resource file persistence object
 * Table: anik_ai_resource
 *
 * Represents file resources in the system
 * Supports associations with multiple storage backends and business types
 * Used to manage avatars, file uploads, material libraries, etc.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_resource")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourcePO {

    /**
     * Resource ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * storage key
     * Unique identifier in the storage backend
     * Local storage: relative path or hash
     * OSS/S3: object key
     */
    private String storageKey;

    /**
     * original file name
     * File name when user uploads
     * Used to display original name when downloading
     */
    private String originalName;

    /**
     * File size (bytes)
     * actual size of file
     */
    private Long fileSize;

    /**
     * MIME type
     * The media type of the file
     * For example: application/pdf, image/png, text/plain
     */
    private String mimeType;

    /**
     * storage type
     * LOCAL: local storage
     * OSS: Alibaba Cloud Object Storage
     * S3: AWS S3
     * AZURE: Microsoft Azure
     */
    private String storageType;

    /**
     * Visit URL
     * External access links to resources
     * Used for front-end direct access or download
     */
    private String accessUrl;

    /**
     * Business type
     * The business type associated with this resource
     * For example: AGENT_AVATAR, USER_AVATAR, SKILL_FILE, etc.
     */
    private String bizType;

    /**
     * Business ID
     * The business object ID associated with this resource
     * If bizType=AGENT_AVATAR, it is Agent ID
     */
    private Long bizId;

    /**
     * Creator userID (foreign key)
     * Linked to anik_ai_user.id
     * The user who uploaded or created the resource
     */
    private Long creatorId;

    /**
     * creation time
     * The moment when the resource is first uploaded/created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time the resource was renewed
     */
    private LocalDateTime updateDt;
}

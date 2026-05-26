package com.aizuda.anik.ai.persistence.skill.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Skill (Skill) information persistence object
 * Table: anik_ai_skill
 *
 * Represents a Skill that can be executed by Agent
 * Skills can contain SKILL.md definitions and supporting files (scripts, References, etc.)
 * Supports reading from DB or file system/object storage
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_skill")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillPO {

    /**
     * SkillID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Skill name
     * Skill display name for users and agents to identify
     */
    private String name;

    /**
     * Skill description
     * Skill function and usage description
     */
    private String description;

    /**
     * File name
     * The name of the original uploaded file (usually package.zip or skill.md)
     */
    private String fileName;

    /**
     * File path (absolute path)
     * The absolute path of the Skill file in the local file system
     * Use this field when not using object storage
     */
    private String filePath;

    /**
     * File size (bytes)
     * Size of compressed package or individual file
     */
    private Long fileSize;

    /**
     * Skill content
     * Skill content defined by SKILL.md
     * Can be stored directly in DB without file system access
     */
    private String skillContent;

    /**
     * Object storage relative path prefix
     * Storage path when using object storage (S3/MinIO/OSS)
     * Format example: skills/123/ or skills/tech-skill-001/v2/
     * When null, it means using the local file system (filePath)
     */
    private String storagePath;

    /**
     * version number
     * Incremented every time the file is renewed
     * Used for cache consistency check and version management
     */
    private Long version;

    /**
     * Whether to include supporting files
     * true: includes additional files such as scripts/, references/, examples/ etc.
     * false: Only SKILL.md can be read directly from DB, skipping file system access
     * Impact performance and access policies
     */
    private Boolean hasFiles;

    /**
     * Creator userID (foreign key)
     * Linked to anik_ai_user.id
     * The creator of this skill
     */
    private Long creatorId;

    /**
     * creation time
     * The moment when the Skill is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The moment when the Skill is renewed for the last time
     */
    private LocalDateTime updateDt;
}

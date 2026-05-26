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
 * Skill supports file persistence objects
 * Table: anik_ai_skill_file
 *
 * Store supporting files (scripts, documents, References, etc.) in the Skill package
 * These files together with the SKILL.md definition form a complete Skill package
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_skill_file")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillFilePO {

    /**
     * File ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SkillID (foreign key)
     * Linked to anik_ai_skill.id
     * Skill package this file belongs to
     */
    private Long skillId;

    /**
     * File relative path
     * Relative path relative to the Skill root directory
     * Examples: scripts/run.py, references/api.md, examples/usage.txt
     * Used to restore directory structure and file organization
     */
    private String filePath;

    /**
     * File content
     * Full content of supporting documents
     * Determine whether to perform base64 encoding based on the encoding field
     * Can be null (points to external storage)
     */
    private String content;

    /**
     * File size (bytes)
     * The original size of the file (before encoding)
     * for display and capacity limitations
     */
    private Integer fileSize;

    /**
     * File encoding
     * utf-8: text file, direct storage
     * base64: Binary file or complex format, base64 encoded
     */
    private String encoding;

    /**
     * creation time
     * The moment when the file is first uploaded to the Skill package
     */
    private LocalDateTime createdAt;

    /**
     * Update time
     * The time when the file was last modified
     */
    private LocalDateTime updatedAt;
}

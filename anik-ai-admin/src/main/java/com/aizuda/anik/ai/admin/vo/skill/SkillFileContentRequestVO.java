package com.aizuda.anik.ai.admin.vo.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Skill file content for GET/PUT /skill/{id}/files/content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillFileContentRequestVO {

    private String content;
    private String encoding; // "utf-8" | "base64"
    private Long size;
}

package com.aizuda.anik.ai.admin.vo.skill;

import lombok.Data;

/**
 * POST /skill/{id}/files request body
 */
@Data
public class SkillCreateFileRequestVO {

    private String path;
    private String type; // "file" | "directory"
}

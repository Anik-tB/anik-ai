package com.aianik.anik.ai.admin.vo.skill;

import lombok.Data;

/**
 * PUT /skill/{id}/files/rename request body
 */
@Data
public class SkillRenameFileRequestVO {

    private String oldPath;
    private String newPath;
}

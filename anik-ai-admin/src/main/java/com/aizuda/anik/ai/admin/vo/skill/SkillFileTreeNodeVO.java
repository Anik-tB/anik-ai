package com.aizuda.anik.ai.admin.vo.skill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Skill file tree node for GET /skill/{id}/files response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillFileTreeNodeVO {

    private String name;
    private String type; // "file" | "directory"
    private Long size;
    private List<SkillFileTreeNodeVO> children;
}

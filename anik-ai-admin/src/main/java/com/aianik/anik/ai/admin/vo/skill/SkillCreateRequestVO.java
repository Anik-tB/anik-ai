package com.aianik.anik.ai.admin.vo.skill;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillCreateRequestVO {

    @NotBlank(message = "name is required")
    private String name;

    private String description;
}

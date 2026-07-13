package com.aianik.anik.ai.admin.vo.agent;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentCreateRequestVO {

    @NotBlank(message = "description is required")
    private String description;
}

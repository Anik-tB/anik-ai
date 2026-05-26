package com.aizuda.anik.ai.common.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Get Skill content request
 *
 * @author openanik
 * @date 2025-04-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillContentRequest {

    private Long skillId;
    private Long agentId;
}

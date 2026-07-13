package com.aianik.anik.ai.common.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Skill content callback response
 *
 * @author openanik
 * @date 2025-04-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillContentResponse {

    private String skillContent;
    private Long version;
    private List<SkillFile> files;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkillFile {
        private String filePath;
        private String content;
        private String encoding;
    }
}

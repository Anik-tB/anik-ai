package com.aizuda.anik.ai.admin.vo.agent;

import com.aizuda.anik.ai.admin.vo.mcp.McpServerResponseVO;
import com.aizuda.anik.ai.admin.vo.skill.SkillResponseVO;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentResponseVO {

    private Long id;

    private String name;

    private String description;

    private String avatar;

    private String instruction;

    private String greeting;

    /**
     * Default question list
     */
    private List<String> presetQuestions;

    private Long chatModelId;

    private String chatModel;

    private Boolean mcpEnabled;

    private List<McpServerResponseVO> mcpServers;

    private Boolean skillEnabled;

    private List<SkillResponseVO> skills;

    private Boolean webSearchEnabled;

    @JsonAlias("knowledgeSpaceEnabled")
    private Boolean ragEnabled;

    /** Whether enabled short-term memory context*/
    private Boolean memoryEnabled;

    /** agent binding RAG ID (stored in agent.config)*/
    @JsonAlias("knowledgeId")
    private Long ragId;

    /** The number of items retained in the short-term memory sliding window*/
    private Integer shortTermMemorySize;

    private String creator;

    private Integer viewCount;

    private Boolean isFeatured;

    /**
     * Agent status
     * @see com.aizuda.anik.ai.common.enums.agent.AgentStatusEnum
     */
    private Integer status;

    /** Associated application ID (NULL=local execution) */
    private String appId;

    private LocalDateTime createDt;

    private LocalDateTime updateDt;

    /** Whether the user has subscribed to this agent (only returned by the market interface)*/
    private Boolean subscribed;
}

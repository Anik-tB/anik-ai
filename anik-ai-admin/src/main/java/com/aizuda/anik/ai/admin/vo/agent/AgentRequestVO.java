package com.aizuda.anik.ai.admin.vo.agent;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class AgentRequestVO {

    private String name;

    private String description;

    private String avatar;

    private String instruction;

    private String greeting;

    /**
     * Default question list (the first question will be synchronized to greeting)
     */
    private List<String> presetQuestions;

    private Long chatModelId;

    private Boolean mcpEnabled;

    private Boolean skillEnabled;

    private Boolean webSearchEnabled;

    @JsonAlias("knowledgeSpaceEnabled")
    private Boolean ragEnabled;

    private Boolean memoryEnabled;

    /** Memory retrieval configuration ID */
    private Long memoryConfigId;

    /** agent binds RAG ID*/
    @JsonAlias("knowledgeId")
    private Long ragId;

    /** The number of short-term memory sliding window reservations, the default is 20*/
    private Integer shortTermMemorySize;

    /** Whether the company recommends it */
    private Boolean isFeatured;

    private List<Long> mcpServerIds;

    private List<Long> skillIds;

    /** Associated application ID (NULL=local execution) */
    private String appId;
}

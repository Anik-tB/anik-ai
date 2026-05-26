package com.aizuda.anik.ai.persistence.agent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * agent (Agent) information persistence object
 * Table: anik_ai_agent
 * <p>
 * Represents an AI assistant/Agent, including its configuration, capabilities, memory, knowledge base and other information
 * Supports multiple capability combinations: MCP, Skill, web search, knowledge base, memory, etc.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_agent")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentPO {

    /**
     * agentID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * agent name
     * User-friendly display name
     */
    private String name;

    /**
     * agent description
     * Briefly describe the functions and uses of the Agent
     */
    private String description;

    /**
     * agent avatar URL
     * The avatar/icon displayed by Agent in the UI
     */
    private String avatar;

    /**
     * system command(System Prompt)
     * A system-level prompt word sent to the large model to define the Agent's role and code of conduct.
     */
    private String instruction;

    /**
     * greeting
     * The welcome message displayed when the user first talks to the Agent will not be displayed as a recommended question.
     */
    private String greeting;

    /**
     * Preset question list (JSON array string)
     * Recommended question button for conversation pages, independent of greeting
     */
    private String presetQuestions;

    /**
     * Chat model ID (foreign key)
     * Related to anik_ai_model_config.id
     * The main LLM model used by this agent
     */
    private Long chatModelId;

    /**
     * Whether enabledMCP (Model Context Protocol)
     * true: enableMCP server integration
     * false: DisableMCP
     */
    private Boolean mcpEnabled;

    /**
     * Whether enabledSkill(Skill)
     * true: Agent can call predefined Skills
     * false: DisableSkill call
     */
    private Boolean skillEnabled;

    /**
     * Whether enabled web search
     * true: Agent can conduct real-time network search
     * false: Disable web search
     */
    private Boolean webSearchEnabled;

    /**
     * Whether enabled RAG
     * true: Agent can retrieve related documents in RAG
     * false: Disable RAG retrieval
     */
    @TableField("rag_enabled")
    private Boolean ragEnabled;

    /**
     * Binding RAG ID (foreign key, can be null)
     * Linked to anik_ai_rag.id
     * Explicitly write NULL when Disable, overriding the overall situation NOT_EMPTY policy
     */
    @TableField("rag_id")
    private Long ragId;

    /**
     * Whether enabled multi-turn dialogue long term memory
     * true: enable long-term memory (retain important information across multiple rounds of dialogue)
     * false: Disable long-term memory
     */
    private Boolean memoryEnabled;

    /**
     * The number of items retained in the short-term memory sliding window
     * Define the number of contextual messages retained across multiple rounds of conversation
     * For example: 10 means to keep the last 10 messages as context
     */
    private Integer shortTermMemorySize;

    /**
     * Creator userID (foreign key, can be null)
     * Linked to anik_ai_user.id
     * The creator of the Agent, used for permission management and traceability
     */
    private Long creatorId;

    /**
     * Is it a recommended agent?
     * true: mark as featured/recommended in the list
     * false: Normal Agent
     */
    private Boolean isFeatured;

    /**
     * Visit statistics
     * The cumulative number of times the Agent has been viewed or used, used for ranking by popularity
     */
    private Integer viewCount;

    /**
     * Agent status
     * @see com.aizuda.anik.ai.common.enums.agent.AgentStatusEnum
     */
    private Integer status;

    /**
     * Configuration JSON (JSONB format)
     * Storage extension configuration parameters
     * For example: {"timeout": 30000, "retryCount": 3}
     */
    private String config;

    /**
     * Associated application ID (foreign key, can be null)
     * Linked to anik_ai_app.app_id
     * NULL: local execution
     * Specific value: Remote application execution (distributed mode)
     */
    private String appId;

    /**
     * creation time
     * The moment when Agent is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time Agent renews
     */
    private LocalDateTime updateDt;
}

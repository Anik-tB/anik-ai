package com.aianik.anik.ai.persistence.agent.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AgentSkill associated persistent objects
 * Table: anik_ai_agent_skill
 *
 * Represents the many-to-many relationship between Agent and Skill
 * An Agent can be bound to multiple Skills and can be called during a conversation.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_agent_skill")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentSkillPO {

    /**
     * Association ID (primary key)
     * auto-increment primary key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent ID (foreign key)
     * Linked to anik_ai_agent.id
     */
    private Long agentId;

    /**
     * Skill ID (foreign key)
     * Linked to anik_ai_skill.id
     */
    private Long skillId;

    /**
     * creation time
     * The moment when the association is created
     */
    private LocalDateTime createDt;
}

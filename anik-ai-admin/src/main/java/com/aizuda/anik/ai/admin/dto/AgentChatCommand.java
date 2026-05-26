package com.aizuda.anik.ai.admin.dto;

import com.aizuda.anik.ai.persistence.admin.po.UserPO;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

/**
 * agent conversation command object
 */
@Data
@Builder
public class AgentChatCommand {

    private Long agentId;
    private String conversationId;
    private String content;
    private List<Long> disabledMcpServerIds;
    private List<Long> disabledSkillIds;
    private ResponseBodyEmitter emitter;
    private UserPO requestUser;
    private String openId;
}

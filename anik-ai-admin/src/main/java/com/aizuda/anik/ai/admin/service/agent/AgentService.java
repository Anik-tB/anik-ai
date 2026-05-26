package com.aizuda.anik.ai.admin.service.agent;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.enums.agent.AgentStatusEnum;
import com.aizuda.anik.ai.common.execption.AnikAiCommonException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.persistence.security.UserSessionUtils;
import com.aizuda.anik.ai.admin.service.model.AiModelConfigService;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigVO;
import com.aizuda.anik.ai.model.enums.ModelTypeEnum;
import com.aizuda.anik.ai.persistence.agent.mapper.*;
import com.aizuda.anik.ai.persistence.memory.mapper.ConversationSummaryMapper;
import com.aizuda.anik.ai.persistence.memory.po.ConversationSummaryPO;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.persistence.agent.po.*;
import com.aizuda.anik.ai.admin.service.mcp.McpServerService;
import com.aizuda.anik.ai.admin.vo.mcp.McpServerResponseVO;
import com.aizuda.anik.ai.admin.service.skill.SkillService;
import com.aizuda.anik.ai.admin.vo.skill.SkillResponseVO;
import com.aizuda.anik.ai.admin.vo.agent.*;
import com.aizuda.anik.ai.admin.vo.memory.ConversationSummaryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.aizuda.anik.ai.common.constants.SystemConstants.YYYY_MM_DD_HH_MM_SS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private static final Long RAG_ID_NONE = 0L;

    private final AgentMapper agentMapper;
    private final AgentConversationMapper agentConversationMapper;
    private final AgentConversationRecordMapper agentConversationRecordMapper;
    private final ConversationSummaryMapper conversationSummaryMapper;
    private final AgentUsageStatMapper agentUsageStatMapper;
    private final AiModelConfigService aiModelConfigService;
    private final McpServerService mcpServerService;
    private final SkillService skillService;

    public PageResult<List<AgentResponseVO>> page(AgentQueryVO query) {
        LambdaQueryWrapper<AgentPO> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(AgentPO::getName, query.getKeyword());
        }
        if (query.getFeatured() != null && query.getFeatured()) {
            wrapper.eq(AgentPO::getIsFeatured, true);
        }

        if ("popular".equals(query.getSort())) {
            wrapper.orderByDesc(AgentPO::getViewCount);
        } else {
            wrapper.orderByDesc(AgentPO::getCreateDt);
        }

        PageDTO<AgentPO> pageDTO = new PageDTO<>(query.getPage(), query.getSize());
        IPage<AgentPO> page = agentMapper.selectPage(pageDTO, wrapper);

        List<AgentResponseVO> records = page.getRecords().stream()
                .map(this::toResponseVO)
                .collect(Collectors.toList());

        return new PageResult<>(pageDTO, records);
    }

    public AgentResponseVO create(AgentRequestVO request) {
        Long userId = UserSessionUtils.currentUserSession().getId();
        
        // Get the default CHAT model
        AiModelConfigVO defaultModel = null;
        if (request.getChatModelId() != null) {
            defaultModel = aiModelConfigService.getModelConfig(request.getChatModelId());
        } else {
            defaultModel = aiModelConfigService.getDefaultModelByType(ModelTypeEnum.CHAT.getValue());
        }
        
        if (defaultModel == null) {
            throw new AnikAiCommonException("No available CHAT model found");
        }
        
        // Build AgentPO
        List<String> normalizedQuestions = normalizePresetQuestions(request.getPresetQuestions());
        
        AgentPO agent = AgentPO.builder()
                .name(request.getName())
                .description(request.getDescription())
                .instruction(request.getInstruction())
                .greeting(request.getGreeting() != null ? request.getGreeting().trim() : null)
                .presetQuestions(normalizedQuestions.isEmpty() ? null : JsonUtil.toJsonString(normalizedQuestions))
                .avatar(request.getAvatar())
                .chatModelId(defaultModel.getId())
                .creatorId(userId)
                .ragId(request.getRagId() != null ? request.getRagId() : RAG_ID_NONE)
                .status(AgentStatusEnum.ACTIVE.getStatus())
                .viewCount(0)
                .mcpEnabled(Boolean.TRUE.equals(request.getMcpEnabled()))
                .skillEnabled(Boolean.TRUE.equals(request.getSkillEnabled()))
                .webSearchEnabled(Boolean.TRUE.equals(request.getWebSearchEnabled()))
                .ragEnabled(Boolean.TRUE.equals(request.getRagEnabled()))
                .memoryEnabled(Boolean.TRUE.equals(request.getMemoryEnabled()))
                .shortTermMemorySize(request.getShortTermMemorySize())
                .isFeatured(false)
                .appId(request.getAppId())
                .build();
        
        agentMapper.insert(agent);
        Long agentId = agent.getId();
        
        if (agentId == null) {
            throw new AnikAiCommonException("Failed to create agent: Unable to get generated ID");
        }
        
        // Associate MCP services
        if (request.getMcpServerIds() != null && !request.getMcpServerIds().isEmpty()) {
            mcpServerService.updateAgentMcpServers(agentId, request.getMcpServerIds());
        }
        
        // Associated Skills
        if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
            skillService.updateAgentSkills(agentId, request.getSkillIds());
        }
        
        // Requery to ensure the latest data is returned
        AgentPO savedAgent = agentMapper.selectById(agentId);
        if (savedAgent == null) {
            throw new AnikAiCommonException("Failed to create the agent: Unable to query the data after saving");
        }
        
        return toResponseVO(savedAgent);
    }

    public AgentResponseVO getById(Long id) {
        AgentPO po = agentMapper.selectById(id);
        if (po == null) {
            throw new AnikAiCommonException("Agent does not exist: {}", id);
        }
        // Increase views
        AgentPO update = new AgentPO();
        update.setId(id);
        update.setViewCount(po.getViewCount() != null ? po.getViewCount() + 1 : 1);
        agentMapper.updateById(update);

        return toResponseVO(po);
    }

    public AgentResponseVO update(Long id, AgentRequestVO request) {
        AgentPO po = agentMapper.selectById(id);
        if (po == null) {
            throw new AnikAiCommonException("Agent does not exist: {}", id);
        }

        if (request.getName() != null) po.setName(request.getName());
        if (request.getDescription() != null) po.setDescription(request.getDescription());
        if (request.getAvatar() != null) po.setAvatar(request.getAvatar());
        if (request.getInstruction() != null) po.setInstruction(request.getInstruction());
        
        // greeting and presetQuestions are processed independently and are no longer synchronized
        if (request.getGreeting() != null) {
            po.setGreeting(request.getGreeting().trim());
        }
        
        if (request.getPresetQuestions() != null) {
            List<String> normalizedPresetQuestions = normalizePresetQuestions(request.getPresetQuestions());
            if (normalizedPresetQuestions.isEmpty()) {
                po.setPresetQuestions(null);
            } else {
                po.setPresetQuestions(JsonUtil.toJsonString(normalizedPresetQuestions));
            }
        }
        if (request.getChatModelId() != null) po.setChatModelId(request.getChatModelId());
        if (request.getMcpEnabled() != null) po.setMcpEnabled(request.getMcpEnabled());
        if (request.getSkillEnabled() != null) po.setSkillEnabled(request.getSkillEnabled());
        if (request.getWebSearchEnabled() != null) po.setWebSearchEnabled(request.getWebSearchEnabled());
        if (request.getRagEnabled() != null) po.setRagEnabled(request.getRagEnabled());
        if (request.getMemoryEnabled() != null) po.setMemoryEnabled(request.getMemoryEnabled());
        if (request.getShortTermMemorySize() != null) po.setShortTermMemorySize(request.getShortTermMemorySize());
        if (request.getIsFeatured() != null) po.setIsFeatured(request.getIsFeatured());
        if (request.getRagId() != null) {
            po.setRagId(request.getRagId());
        }
        if (Boolean.FALSE.equals(request.getRagEnabled())) {
            po.setRagId(RAG_ID_NONE);
        }
        // appId is allowed to be set to null (clear = local execution)
        po.setAppId(request.getAppId());

        agentMapper.updateById(po);

        //renew MCP service association
        if (request.getMcpServerIds() != null) {
            mcpServerService.updateAgentMcpServers(id, request.getMcpServerIds());
        }

        //renew Skill association
        if (request.getSkillIds() != null) {
            skillService.updateAgentSkills(id, request.getSkillIds());
        }

        return toResponseVO(po);
    }

    @Transactional
    public void delete(Long id) {
        AgentPO po = agentMapper.selectById(id);
        if (po == null) {
            return;
        }

        //delete associated data
        agentConversationRecordMapper.delete(
                new LambdaQueryWrapper<AgentConversationRecordPO>().eq(AgentConversationRecordPO::getAgentId, id));
        agentConversationMapper.delete(
                new LambdaQueryWrapper<AgentConversationPO>().eq(AgentConversationPO::getAgentId, id));
        agentUsageStatMapper.delete(
                new LambdaQueryWrapper<AgentUsageStatPO>().eq(AgentUsageStatPO::getAgentId, id));
        agentMapper.deleteById(id);
    }

    /**
     * Delete a single conversation and its associated data (do not delete long-term memory {@code anik_ai_conversation_memory})
     */
    @Transactional
    public void deleteConversation(Long agentId, String conversationId) {
        Long userId = UserSessionUtils.currentUserSession().getId();
        AgentConversationPO conversation = agentConversationMapper.selectOne(
                new LambdaQueryWrapper<AgentConversationPO>()
                        .eq(AgentConversationPO::getConversationId, conversationId)
                        .eq(AgentConversationPO::getAgentId, agentId)
                        .eq(AgentConversationPO::getUserId, userId));
        if (conversation == null) {
            throw new AnikAiCommonException("The conversation does not exist or you do not have permission to delete it.");
        }

        agentConversationRecordMapper.delete(
                new LambdaQueryWrapper<AgentConversationRecordPO>()
                        .eq(AgentConversationRecordPO::getConversationId, conversationId)
                        .eq(AgentConversationRecordPO::getAgentId, agentId));
        conversationSummaryMapper.delete(
                new LambdaQueryWrapper<ConversationSummaryPO>()
                        .eq(ConversationSummaryPO::getConversationId, conversationId)
                        .eq(ConversationSummaryPO::getAgentId, agentId));
        agentConversationMapper.deleteById(conversation.getId());
        log.info("Delete conversation successfully: agentId={}, conversationId={}, userId={}", agentId, conversationId, userId);
    }

    @Transactional
    public void batchDeleteConversations(Long agentId, List<String> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return;
        }
        for (String conversationId : conversationIds) {
            deleteConversation(agentId, conversationId);
        }
    }

    /**
     * Page query the session list of the current user under the specified Agent ({@code anik_ai_agent_conversation} + record table aggregation statistics)
     */
    public PageResult<List<ConversationSummaryVO>> listConversations(Long agentId, AgentConversationQueryVO query) {
        Long userId = UserSessionUtils.currentUserSession().getId();
        String userName = UserSessionUtils.currentUserSession().getUsername();

        LambdaQueryWrapper<AgentConversationPO> wrapper = new LambdaQueryWrapper<AgentConversationPO>()
                .eq(AgentConversationPO::getAgentId, agentId)
                .eq(AgentConversationPO::getUserId, userId);

        if (ObjUtil.isNotNull(query.getStartDt()) && ObjUtil.isNotNull(query.getEndDt())) {
            wrapper.between(AgentConversationPO::getCreateDt, query.getStartDt(), query.getEndDt());
        } else if (StrUtil.isNotBlank(query.getStart()) && StrUtil.isNotBlank(query.getEnd())) {
            LocalDate start = LocalDate.parse(query.getStart().trim());
            LocalDate end = LocalDate.parse(query.getEnd().trim());
            if (start.isAfter(end)) {
                LocalDate t = start;
                start = end;
                end = t;
            }
            wrapper.ge(AgentConversationPO::getCreateDt, start.atStartOfDay())
                    .le(AgentConversationPO::getCreateDt, LocalDateTime.of(end, LocalTime.MAX));
        }

        wrapper.orderByDesc(AgentConversationPO::getUpdateDt);

        Page<AgentConversationPO> pageParam = new Page<>(query.getPage(), query.getSize());
        Page<AgentConversationPO> page = agentConversationMapper.selectPage(pageParam, wrapper);

        List<String> convIds = page.getRecords().stream()
                .map(AgentConversationPO::getConversationId)
                .collect(Collectors.toList());

        Map<String, ConversationStats> statsByConvId = new HashMap<>();
        if (!convIds.isEmpty()) {
            List<AgentConversationRecordPO> records = agentConversationRecordMapper.selectList(
                    new LambdaQueryWrapper<AgentConversationRecordPO>()
                            .select(AgentConversationRecordPO::getConversationId, AgentConversationRecordPO::getCreateDt)
                            .eq(AgentConversationRecordPO::getAgentId, agentId)
                            .eq(AgentConversationRecordPO::getUserId, userId)
                            .in(AgentConversationRecordPO::getConversationId, convIds));
            for (AgentConversationRecordPO record : records) {
                String conversationId = record.getConversationId();
                if (conversationId == null) {
                    continue;
                }
                statsByConvId.merge(conversationId,
                        new ConversationStats(1, record.getCreateDt()),
                        AgentService::mergeConversationStats);
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        List<ConversationSummaryVO> rows = page.getRecords().stream().map(conv -> {
            ConversationStats st = statsByConvId.get(conv.getConversationId());
            int messageCount = st != null ? st.messageCount() : 0;
            LocalDateTime lastMsg = st != null && st.lastMessageDt() != null
                    ? st.lastMessageDt()
                    : conv.getUpdateDt();
            return ConversationSummaryVO.builder()
                    .conversationId(conv.getConversationId())
                    .title(conv.getTitle())
                    .userName(userName)
                    .messageCount(messageCount)
                    .toolCallCount(0)
                    .createDt(conv.getCreateDt() != null ? conv.getCreateDt().format(dtf) : null)
                    .lastMessageDt(lastMsg != null ? lastMsg.format(dtf) : null)
                    .build();
        }).collect(Collectors.toList());

        return new PageResult<>(page, rows);
    }

    private record ConversationStats(int messageCount, LocalDateTime lastMessageDt) {
    }

    private static ConversationStats mergeConversationStats(ConversationStats left, ConversationStats right) {
        return new ConversationStats(
                left.messageCount() + right.messageCount(),
                laterOf(left.lastMessageDt(), right.lastMessageDt()));
    }

    private static LocalDateTime laterOf(LocalDateTime a, LocalDateTime b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }

    /**
     * Get a list of available CHAT type models
     */
    public List<Map<String, Object>> getChatModels() {
        List<AiModelConfigVO> models = aiModelConfigService.getModelsByType(ModelTypeEnum.CHAT.getValue());
        return models.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", m.getModelKey());
            map.put("label", m.getModelName());
            map.put("id", m.getId());
            return map;
        }).collect(Collectors.toList());
    }

    public AgentResponseVO toResponseVO(AgentPO po) {
        String chatModelName = null;
        if (po.getChatModelId() != null) {
            AiModelConfigVO modelConfig = aiModelConfigService.getModelConfig(po.getChatModelId());
            if (modelConfig != null) {
                chatModelName = modelConfig.getModelKey();
            }
        }

        // Query the associated MCP service
        List<McpServerResponseVO> mcpServers = null;
        if (Boolean.TRUE.equals(po.getMcpEnabled())) {
            mcpServers = mcpServerService.getByAgentId(po.getId());
        }

        // Query the associated Skill
        List<SkillResponseVO> skills = null;
        if (Boolean.TRUE.equals(po.getSkillEnabled())) {
            skills = skillService.getByAgentId(po.getId());
        }

        List<String> presetQuestions = resolvePresetQuestions(po);

        return AgentResponseVO.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .avatar(po.getAvatar())
                .instruction(po.getInstruction())
                .greeting(po.getGreeting())
                .presetQuestions(presetQuestions)
                .chatModelId(po.getChatModelId())
                .chatModel(chatModelName)
                .mcpEnabled(po.getMcpEnabled())
                .mcpServers(mcpServers)
                .skillEnabled(po.getSkillEnabled())
                .skills(skills)
                .webSearchEnabled(po.getWebSearchEnabled())
                .ragEnabled(po.getRagEnabled())
                .memoryEnabled(po.getMemoryEnabled())
                .ragId(RAG_ID_NONE.equals(po.getRagId()) ? null : po.getRagId())
                .shortTermMemorySize(po.getShortTermMemorySize())
                .creator(null)
                .viewCount(po.getViewCount())
                .isFeatured(po.getIsFeatured())
                .status(po.getStatus())
                .appId(po.getAppId())
                .createDt(po.getCreateDt())
                .updateDt(po.getUpdateDt())
                .build();
    }

    private List<String> resolvePresetQuestions(AgentPO po) {
        if (StrUtil.isNotBlank(po.getPresetQuestions())) {
            try {
                List<String> parsed = JsonUtil.parseList(po.getPresetQuestions(), String.class);
                List<String> normalized = normalizePresetQuestions(parsed);
                if (!normalized.isEmpty()) {
                    return normalized;
                }
            } catch (Exception e) {
                log.warn("Failed to parse agent default problem: agentId={}", po.getId(), e);
            }
        }
        // No more returning to the source from greeting, maintaining independence
        return List.of();
    }

    private List<String> normalizePresetQuestions(List<String> questions) {
        if (questions == null || questions.isEmpty()) {
            return List.of();
        }
        return questions.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
    }

}

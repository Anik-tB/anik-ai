package com.aizuda.anik.ai.admin.service.agent;

import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.admin.vo.agent.AgentAnalyticsQueryVO;
import com.aizuda.anik.ai.admin.vo.agent.AgentAnalyticsVO;
import com.aizuda.anik.ai.admin.vo.agent.AgentUsageDetailQueryVO;
import com.aizuda.anik.ai.admin.vo.agent.AgentUsageDetailVO;
import com.aizuda.anik.ai.persistence.agent.mapper.AgentUsageStatMapper;
import com.aizuda.anik.ai.persistence.agent.po.AgentUsageStatPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentAnalyticsService {

    private final AgentUsageStatMapper usageStatMapper;
    /**
     * Get an analysis overview
     */
    public AgentAnalyticsVO getAnalytics(Long agentId, AgentAnalyticsQueryVO queryVO) {
        String range = queryVO.getRange();
        String start = queryVO.getStart();
        String end = queryVO.getEnd();
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        boolean hasCustomDateRange = start != null && !start.isBlank() && end != null && !end.isBlank();
        if (hasCustomDateRange) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
            if (startDate.isAfter(endDate)) {
                LocalDate temp = startDate;
                startDate = endDate;
                endDate = temp;
            }
        } else {
            int days = switch (range) {
                case "7d" -> 7;
                case "30d" -> 30;
                default -> 1;
            };
            startDate = endDate.minusDays(days - 1);
        }

        LambdaQueryWrapper<AgentUsageStatPO> wrapper = new LambdaQueryWrapper<AgentUsageStatPO>()
                .eq(AgentUsageStatPO::getAgentId, agentId)
                .ge(AgentUsageStatPO::getStatDate, startDate)
                .le(AgentUsageStatPO::getStatDate, endDate);

        List<AgentUsageStatPO> stats = usageStatMapper.selectList(wrapper);

        // aggregate statistics
        Set<Long> uniqueUsers = new HashSet<>();
        int totalMessages = 0;
        int totalConversations = 0;

        for (AgentUsageStatPO stat : stats) {
            uniqueUsers.add(stat.getUserId());
            totalMessages += stat.getMessageCount() != null ? stat.getMessageCount() : 0;
            totalConversations += stat.getConversationCount() != null ? stat.getConversationCount() : 0;
        }

        Map<LocalDate, Integer> usersByDate = new HashMap<>();
        Map<LocalDate, Integer> convByDate = new HashMap<>();
        Map<LocalDate, Integer> messageByDate = new HashMap<>();

        for (AgentUsageStatPO stat : stats) {
            usersByDate.merge(stat.getStatDate(), 1, Integer::sum);
            convByDate.merge(stat.getStatDate(), stat.getConversationCount() != null ? stat.getConversationCount() : 0, Integer::sum);
            messageByDate.merge(stat.getStatDate(), stat.getMessageCount() != null ? stat.getMessageCount() : 0, Integer::sum);
        }

        List<String> dateLabels = new ArrayList<>();
        List<Integer> activeUsersTrend = new ArrayList<>();
        List<Integer> conversationTrend = new ArrayList<>();
        List<Integer> messageTrend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dateLabels.add(current.format(fmt));
            activeUsersTrend.add(usersByDate.getOrDefault(current, 0));
            conversationTrend.add(convByDate.getOrDefault(current, 0));
            messageTrend.add(messageByDate.getOrDefault(current, 0));
            current = current.plusDays(1);
        }

        int totalToolCalls = 0;
        double avgResponseTime = 0;
//        try {
//            LocalDateTime rangeStart = startDate.atStartOfDay();
//            LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay();
//
//// Query from the new observation system: Query observation through trace table association
//// 1. First query all traces of the agent within the time range
//            List<TracePO> traces = traceMapper.selectList(
//                    new LambdaQueryWrapper<TracePO>()
//                            .eq(TracePO::getAgentId, agentId)
//                            .ge(TracePO::getCreateDt, rangeStart)
//                            .le(TracePO::getCreateDt, rangeEnd));
//
//            if (!traces.isEmpty()) {
//                Set<String> traceIds = new HashSet<>();
//                traces.forEach(t -> traceIds.add(t.getId()));
//
//                // // 2. Query the number of tool calls (type = TOOL)
//                totalToolCalls = observationMapper.selectCount(
//                        new LambdaQueryWrapper<ObservationPO>()
//                                .in(ObservationPO::getTraceId, traceIds)
//                                .eq(ObservationPO::getType, ObservationTypeEnum.TOOL.getType()))
//                        .intValue();
//
//                // // 3. Query model call (type = GENERATION) to calculate the average response time
//                List<ObservationPO> generationObservations = observationMapper.selectList(
//                        new LambdaQueryWrapper<ObservationPO>()
//                                .in(ObservationPO::getTraceId, traceIds)
//                                .eq(ObservationPO::getType, ObservationTypeEnum.GENERATION.getType())
//                                .isNotNull(ObservationPO::getStartTime)
//                                .isNotNull(ObservationPO::getEndTime));
//
//                if (!generationObservations.isEmpty()) {
//                    long totalDurationMs = generationObservations.stream()
//                            .mapToLong(obs -> {
//                                LocalDateTime start1 = obs.getStartTime();
//                                LocalDateTime end1 = obs.getEndTime();
//                                if (start1 != null && end1 != null) {
//                                    return java.time.Duration.between(start1, end1).toMillis();
//                                }
//                                return 0L;
//                            })
//                            .sum();
//                    avgResponseTime = (double) totalDurationMs / generationObservations.size();
//                }
//            }
//        } catch (Exception e) {
//            log.warn("Failed to query observation statistics agentId={}", agentId, e);
//        }

        return AgentAnalyticsVO.builder()
                .activeUsers(uniqueUsers.size())
                .activeUsersTrend(activeUsersTrend)
                .conversationCount(totalConversations)
                .conversationCountTrend(conversationTrend)
                .totalMessages(totalMessages)
                .dateLabels(dateLabels)
                .messageTrend(messageTrend)
                .totalToolCalls(totalToolCalls)
                .avgResponseTime(avgResponseTime)
                .dateRange(AgentAnalyticsVO.DateRange.builder()
                        .start(startDate.format(fmt))
                        .end(endDate.format(fmt))
                        .build())
                .build();
    }

    /**
     * Get usage details
     */
    public PageResult<List<AgentUsageDetailVO>> getUsageDetail(Long agentId, AgentUsageDetailQueryVO queryVO) {
        LambdaQueryWrapper<AgentUsageStatPO> wrapper = new LambdaQueryWrapper<AgentUsageStatPO>()
                .eq(AgentUsageStatPO::getAgentId, agentId);
        String start = queryVO.getStart();
        String end = queryVO.getEnd();
        int page = queryVO.getPage();
        int size = queryVO.getSize();

        if (start != null) {
            wrapper.ge(AgentUsageStatPO::getStatDate, LocalDate.parse(start));
        }
        if (end != null) {
            wrapper.le(AgentUsageStatPO::getStatDate, LocalDate.parse(end));
        }

        //First check the full data and group it by user
        List<AgentUsageStatPO> allStats = usageStatMapper.selectList(wrapper);

        Map<Long, AgentUsageDetailVO> userMap = new LinkedHashMap<>();
        for (AgentUsageStatPO stat : allStats) {
            userMap.merge(stat.getUserId(),
                    AgentUsageDetailVO.builder()
                            .userId(stat.getUserId())
                            .userName(stat.getUserName())
                            .department(stat.getDepartment())
                            .messageCount(stat.getMessageCount() != null ? stat.getMessageCount() : 0)
                            .build(),
                    (existing, newVal) -> {
                        existing.setMessageCount(existing.getMessageCount() + newVal.getMessageCount());
                        return existing;
                    });
        }

        List<AgentUsageDetailVO> allDetails = new ArrayList<>(userMap.values());

        // Manual paging
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allDetails.size());
        List<AgentUsageDetailVO> pageData = fromIndex < allDetails.size()
                ? allDetails.subList(fromIndex, toIndex)
                : List.of();

        PageDTO<AgentUsageStatPO> pageDTO = new PageDTO<>(page, size);
        pageDTO.setTotal(allDetails.size());

        return new PageResult<>(pageDTO, pageData);
    }
}

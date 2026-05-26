package com.aizuda.anik.ai.admin.vo.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentAnalyticsVO {

    private Integer activeUsers;

    private List<Integer> activeUsersTrend;

    private Integer conversationCount;

    private List<Integer> conversationCountTrend;

    /** Total number of messages */
    private Integer totalMessages;

    /** Date label, one-to-one correspondence with trend data */
    private List<String> dateLabels;

    /** Message trends (by day) */
    private List<Integer> messageTrend;

    /** Total number of tool calls */
    private Integer totalToolCalls;

    /** Average response time (ms) */
    private Double avgResponseTime;

    private DateRange dateRange;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DateRange {
        private String start;
        private String end;
    }
}

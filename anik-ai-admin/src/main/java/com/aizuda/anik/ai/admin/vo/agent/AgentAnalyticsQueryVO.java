package com.aizuda.anik.ai.admin.vo.agent;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentAnalyticsQueryVO extends BaseQueryVO {

    private String range = "7d";

    private String start;

    private String end;
}

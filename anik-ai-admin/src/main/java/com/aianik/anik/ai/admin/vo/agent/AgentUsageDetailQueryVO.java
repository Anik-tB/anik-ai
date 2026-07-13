package com.aianik.anik.ai.admin.vo.agent;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentUsageDetailQueryVO extends BaseQueryVO {

    private String start;

    private String end;
}

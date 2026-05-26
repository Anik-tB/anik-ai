package com.aizuda.anik.ai.admin.vo.agent;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentQueryVO extends BaseQueryVO {

    private String keyword;

    private Boolean featured;

    /** latest / popular */
    private String sort;
}

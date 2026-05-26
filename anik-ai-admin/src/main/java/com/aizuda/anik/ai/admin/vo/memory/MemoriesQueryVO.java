package com.aizuda.anik.ai.admin.vo.memory;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MemoriesQueryVO extends BaseQueryVO {

    private Long agentId;
    private Long userId;
    private String conversationId;
    private Integer status;

}

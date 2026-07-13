package com.aianik.anik.ai.admin.vo.memory;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
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

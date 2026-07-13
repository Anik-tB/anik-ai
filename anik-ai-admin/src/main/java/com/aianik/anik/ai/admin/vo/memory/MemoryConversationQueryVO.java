package com.aianik.anik.ai.admin.vo.memory;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryConversationQueryVO extends BaseQueryVO {
    private Integer limit = 10;
}

package com.aizuda.anik.ai.admin.vo.memory;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryRecentQueryVO extends BaseQueryVO {
    private Integer days = 7;
    private Integer limit = 10;
}

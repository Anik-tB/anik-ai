package com.aianik.anik.ai.admin.vo.memory;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryStatsQueryVO extends BaseQueryVO {
    private Integer days = 30;
}

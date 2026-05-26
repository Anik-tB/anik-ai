package com.aizuda.anik.ai.admin.vo;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreInstanceQueryVO extends BaseQueryVO {

    private Integer category;
}

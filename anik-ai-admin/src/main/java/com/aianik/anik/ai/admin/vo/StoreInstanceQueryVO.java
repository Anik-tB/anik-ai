package com.aianik.anik.ai.admin.vo;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreInstanceQueryVO extends BaseQueryVO {

    private Integer category;
}

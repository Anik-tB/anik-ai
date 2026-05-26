package com.aizuda.anik.ai.admin.vo.knowledge;

import com.aizuda.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeQueryVO extends BaseQueryVO {

    private String name;
}

package com.aianik.anik.ai.admin.vo.knowledge;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeQueryVO extends BaseQueryVO {

    private String name;
}

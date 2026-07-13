package com.aianik.anik.ai.admin.vo.skill;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkillQueryVO extends BaseQueryVO {

    private String keyword;
}

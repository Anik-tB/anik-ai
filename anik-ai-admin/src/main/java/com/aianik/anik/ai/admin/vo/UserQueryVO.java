package com.aianik.anik.ai.admin.vo;

import com.aianik.anik.ai.common.vo.BaseQueryVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * author: openanik
 * date: 2025-07-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryVO extends BaseQueryVO {

    private String email;
}

package com.aizuda.anik.ai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Common status enum (Enabled/Disabled)
 *
 * @author openanik
 * @date 2025-04-11
 */
@AllArgsConstructor
@Getter
public enum CommonStatusEnum {

    DISABLED(0, "Disable"),
    ENABLED(1, "enable");

    @EnumValue
    private final Integer value;
    private final String desc;
}

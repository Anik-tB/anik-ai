package com.aizuda.anik.ai.common.enums.memory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Memory retrieval rule instruction type
 */
@Getter
@AllArgsConstructor
public enum ExtractionRuleTypeEnum {

    /** Default built-in rules (recommended, suitable for most scenarios) */
    DEFAULT("DEFAULT", "Default rule directive"),

    /** Custom rule instructions (user fills in focus/ignore rules) */
    CUSTOM("CUSTOM", "Custom rule instructions");

    @EnumValue
    private final String code;
    private final String description;

    public static ExtractionRuleTypeEnum fromCode(String code) {
        if (code == null) return DEFAULT;
        for (ExtractionRuleTypeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) return e;
        }
        return DEFAULT;
    }
}

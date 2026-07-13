package com.aianik.anik.ai.common.enums.memory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryTypeEnum {

    FACT(1, "fact"),
    DECISION(2, "decision making"),
    PREFERENCE(3, "Preference"),
    TASK_PROGRESS(4, "Task progress"),
    REFERENCE(5, "References");

    @EnumValue
    private final Integer type;
    private final String description;

    public static MemoryTypeEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }
        for (MemoryTypeEnum e : values()) {
            if (e.type.equals(type)) {
                return e;
            }
        }
        return null;
    }

    public static MemoryTypeEnum fromLegacy(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim().toUpperCase()) {
            case "FACT" -> FACT;
            case "DECISION" -> DECISION;
            case "PREFERENCE" -> PREFERENCE;
            case "TASK_PROGRESS" -> TASK_PROGRESS;
            case "REFERENCE" -> REFERENCE;
            default -> null;
        };
    }
}

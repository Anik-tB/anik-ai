package com.aizuda.anik.ai.common.enums.memory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * memory state enum
 */
@Getter
@AllArgsConstructor
public enum MemoryStatusEnum {

    ACTIVE(1, "activation"),
    ARCHIVED(2, "Archived"),
    SUPPRESSED(3, "Suppressed");

    /** Value mapped to database by MyBatis-Plus */
    @EnumValue
    private final Integer status;
    private final String description;

    public static MemoryStatusEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (MemoryStatusEnum e : values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }

    public static MemoryStatusEnum fromLegacy(String status) {
        if (status == null) {
            return null;
        }
        return switch (status.trim().toUpperCase()) {
            case "ACTIVE" -> ACTIVE;
            case "ARCHIVED" -> ARCHIVED;
            case "SUPPRESSED" -> SUPPRESSED;
            default -> null;
        };
    }
}

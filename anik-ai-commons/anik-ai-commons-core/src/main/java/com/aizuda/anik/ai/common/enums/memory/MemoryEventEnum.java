package com.aizuda.anik.ai.common.enums.memory;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemoryEventEnum {

    ADD(1, "New"),
    UPDATE(2, "renew"),
    DELETE(3, "delete"),
    NOOP(4, "No action");

    @EnumValue
    private final Integer event;
    private final String description;

    public static MemoryEventEnum fromEvent(Integer event) {
        if (event == null) {
            return null;
        }
        for (MemoryEventEnum e : values()) {
            if (e.event.equals(event)) {
                return e;
            }
        }
        return null;
    }

    public static MemoryEventEnum fromLegacy(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim().toUpperCase()) {
            case "ADD" -> ADD;
            case "UPDATE" -> UPDATE;
            case "DELETE" -> DELETE;
            case "NOOP" -> NOOP;
            default -> null;
        };
    }
}

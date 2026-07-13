package com.aianik.anik.ai.common.enums.agent;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent status enumeration
 */
@Getter
@AllArgsConstructor
public enum AgentStatusEnum {

    ACTIVE(1, "active"),
    INACTIVE(2, "inactive"),
    DEPRECATED(3, "Deprecated"),
    DISABLED(4, "Disabled");

    /** Value mapped to database by MyBatis-Plus */
    @EnumValue
    private final Integer status;
    private final String description;

    /**
     * Get enum by status value
     */
    public static AgentStatusEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (AgentStatusEnum e : values()) {
            if (e.status.equals(status)) {
                return e;
            }
        }
        return null;
    }
}

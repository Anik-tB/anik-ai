package com.aizuda.anik.ai.model.enums;

/**
 * Model state enum
 */
public enum ModelStatusEnum {
    /**
     * Enabled
     */
    ENABLED(1, "Enabled"),

    /**
     * Disabled
     */
    DISABLED(0, "Disabled");

    private final int code;
    private final String description;

    ModelStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ModelStatusEnum fromCode(int code) {
        for (ModelStatusEnum status : ModelStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

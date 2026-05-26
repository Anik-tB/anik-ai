package com.aizuda.anik.ai.model.enums;

/**
 * Model scope enum
 */
public enum ModelScopeEnum {
    /**
     * overall situation model - Admin configuration
     */
    GLOBAL("GLOBAL", "overall situation"),

    /**
     * personal model - userpersonal configuration
     */
    PERSONAL("PERSONAL", "personal");

    private final String value;
    private final String name;

    ModelScopeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static ModelScopeEnum fromValue(String value) {
        for (ModelScopeEnum scope : ModelScopeEnum.values()) {
            if (scope.value.equals(value)) {
                return scope;
            }
        }
        return null;
    }
}

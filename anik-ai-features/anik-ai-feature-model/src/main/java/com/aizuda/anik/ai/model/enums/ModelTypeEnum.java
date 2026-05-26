package com.aizuda.anik.ai.model.enums;

/**
 * Model type enumeration
 * Supports classification of multiple AI models
 */
public enum ModelTypeEnum {
    /**
     * dialogue model - for dialogue, question and answer, and text generation
     */
    CHAT("CHAT", "dialogue model"),

    /**
     * vector model - used for text vectorization and similarity calculation
     */
    EMBEDDING("EMBEDDING", "vector model"),

    /**
     * rearrange model - used to rearrange search results
     */
    RERANKER("RERANKER", "rearrange model"),

    /**
     * image model - for image generation and understanding
     */
    IMAGE("IMAGE", "image model"),

    /**
     * speech model - for speech recognition and generation
     */
    SPEECH("SPEECH", "speech model");

    private final String value;
    private final String name;

    ModelTypeEnum(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static ModelTypeEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ModelTypeEnum type : ModelTypeEnum.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}

package com.aizuda.anik.ai.features.rag.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChunkModeEnum {

    /** Slice by length (pure recursive slicing, no first-level separation) */
    DEFAULT("default"),
    /** Slice by delimiter (first split by delimiter level, then recursively) */
    DELIMITER("delimiter"),
    /** Regular expression slicing (first split according to regular expression level, then recursive)*/
    REGEX("regex"),
    /** Intelligent slicing (LLM semantic segmentation, recursion)*/
    SMART("smart");

    private final String mode;

    /**
     * Parses an enumeration based on its string value, case-insensitively; returns DEFAULT if no match is found.
     */
    public static ChunkModeEnum fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }
        for (ChunkModeEnum e : values()) {
            if (e.mode.equalsIgnoreCase(value.trim())) {
                return e;
            }
        }
        return DEFAULT;
    }
}

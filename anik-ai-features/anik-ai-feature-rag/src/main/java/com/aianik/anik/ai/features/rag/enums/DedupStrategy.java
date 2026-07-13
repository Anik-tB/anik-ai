package com.aianik.anik.ai.features.rag.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Knowledge base document uploading deduplication strategy
 *
 * @author openanik
 */
@Getter
@AllArgsConstructor
public enum DedupStrategy {

    /** Do not remove duplicates */
    NONE(0),

    /** Files with the same name in the same library are considered duplicates */
    BY_NAME(1),

    /** The same SHA-256 contents in the same library are considered duplicates */
    BY_CONTENT(2),

    /** Any hits with the same name or the same content will be regarded as duplicates */
    BY_NAME_OR_CONTENT(3);

    @EnumValue
    private final Integer code;

    public boolean matchesByName() {
        return this == BY_NAME || this == BY_NAME_OR_CONTENT;
    }

    public boolean matchesByContent() {
        return this == BY_CONTENT || this == BY_NAME_OR_CONTENT;
    }

    public static DedupStrategy fromCode(Integer code) {
        if (code == null) {
            return BY_CONTENT;
        }
        for (DedupStrategy e : values()) {
            if (Objects.equals(e.code, code)) {
                return e;
            }
        }
        return BY_CONTENT;
    }
}

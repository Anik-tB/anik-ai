package com.aianik.anik.ai.features.rag.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * Processing actions during hit deduplication
 *
 * @author openanik
 */
@Getter
@AllArgsConstructor
public enum DedupAction {

    /**Reject and report an error*/
    REJECT(0),

    /** Skip this upload without reporting an error */
    SKIP(1),

    /** Delete old documents (including chunks/vectors) and reprocess them as new files*/
    OVERWRITE(2);

    @EnumValue
    private final Integer code;

    public static DedupAction fromCode(Integer code) {
        if (code == null) {
            return REJECT;
        }
        for (DedupAction e : values()) {
            if (Objects.equals(e.code, code)) {
                return e;
            }
        }
        return REJECT;
    }
}

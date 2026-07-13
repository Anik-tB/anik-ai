package com.aianik.anik.ai.features.rag.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Article processing status
 * </p>
 *
 * @author openanik
 * @date 2025-07-19
 */
@AllArgsConstructor
@Getter
public enum DocumentStatus {
    INIT(0),
    PROCESSING(1),
    SUCCESS(2),
    FAIL(3);

    private final int status;

}

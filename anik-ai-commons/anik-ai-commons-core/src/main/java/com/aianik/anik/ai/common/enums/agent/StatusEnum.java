package com.aianik.anik.ai.common.enums.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Agent conversation record and other status
 *
 * @author openanik
 * @date 2025-07-09
 */
@AllArgsConstructor
@Getter
public enum StatusEnum {

    RUNNING(1),
    SUCCESS(2),
    FAIL(3);

    private final Integer value;
}

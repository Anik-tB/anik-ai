package com.aianik.anik.ai.features.resource.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceBizTypeEnum {

    GENERAL("GENERAL"),
    AVATAR("AVATAR"),
    ATTACHMENT("ATTACHMENT"),
    DOCUMENT("DOCUMENT"),
    /** Temporary resources in the preview phase of the knowledge base upload; promoted to DOCUMENT by reference when committing, and cleared when cancel/timeout */
    DOCUMENT_PREVIEW("DOCUMENT_PREVIEW");

    private final String value;
}

package com.aianik.anik.ai.common.enums.rag;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatusEnum {

    PENDING(0, "Pending"),
    PARSING(1, "Parsing"),
    PROCESSING(2, "Processing"),
    SUCCESS(3, "Processing completed"),
    FAILED(4, "Processing failed");

    @EnumValue
    private final Integer status;
    private final String description;
}

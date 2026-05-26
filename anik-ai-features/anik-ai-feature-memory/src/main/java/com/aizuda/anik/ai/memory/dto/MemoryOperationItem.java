package com.aizuda.anik.ai.memory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single memory operation result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryOperationItem {

    private Long memoryId;

    private String vectorId;

    private String memory;
}

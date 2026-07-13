package com.aianik.anik.ai.admin.vo.memory;

import com.aianik.anik.ai.memory.dto.MemoryMetrics;
import com.aianik.anik.ai.memory.dto.MemoryOperationItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemoryResultResponseVO {

    private List<MemoryOperationItem> operations;
    private MemoryMetrics metrics;
}

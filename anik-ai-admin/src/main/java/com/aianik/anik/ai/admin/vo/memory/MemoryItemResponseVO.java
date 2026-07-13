package com.aianik.anik.ai.admin.vo.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * External unified memory view (facade API)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryItemResponseVO {

    private Long id;
    private String vectorId;
    private String title;
    private String content;
    private Integer memoryType;
    private List<String> tags;
    private Double relevanceScore;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.aianik.anik.ai.memory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Short-term memory history query parameter
 *
 * @author openanik
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortTermHistoryQuery {

    private String conversationId;

    private Long agentId;

    private Long userId;

    private Integer shortTermMemorySize;
}

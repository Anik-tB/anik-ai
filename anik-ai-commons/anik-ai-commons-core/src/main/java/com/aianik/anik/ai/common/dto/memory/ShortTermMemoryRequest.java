package com.aianik.anik.ai.common.dto.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Load short-term conversation history request
 *
 * @author openanik
 * @date 2025-04-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortTermMemoryRequest {

    private String conversationId;
    private Integer windowSize;
}

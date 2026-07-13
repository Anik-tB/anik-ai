package com.aianik.anik.ai.common.grpc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server-side timestamp response (generic response object)
 *
 * @author openanik
 * @date 2025-04-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerTimestampResponse {
    
    private Long serverTimestamp;
}

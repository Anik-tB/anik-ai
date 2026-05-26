package com.aizuda.anik.ai.common.dto.beat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Heartbeat reporting data
 * <p>
 * author: zhangshuguang
 * date: 2026-05-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatBodyRequest {
    private int maxConcurrentChats;
    private int activeChats;
}

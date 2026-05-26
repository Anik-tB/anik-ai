package com.aizuda.anik.ai.common.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Heartbeat reporting data
 *
 * @author openanik
 * @date 2025-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatBody {

    private int maxConcurrentChats;
    private int activeChats;
}

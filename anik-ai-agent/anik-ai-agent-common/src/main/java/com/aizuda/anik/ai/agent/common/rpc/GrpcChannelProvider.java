package com.aizuda.anik.ai.agent.common.rpc;

import com.aizuda.anik.ai.agent.common.exception.CallbackChannelUnavailableException;
import io.grpc.ManagedChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * gRPC Channel life cycle management
 * <p>
 * Responsible for managing gRPC connections and request headers with Server, supporting runtime refresh
 *
 * @author openanik
 * @date 2025-04-12
 */
@Component
@Slf4j
public class GrpcChannelProvider {
    
    private volatile ManagedChannel channel;
    /**
     * -- GETTER --
     *  Get request headers
     */
    @Getter
    private volatile Map<String, String> headers = Map.of();
    
    /**
     * renew channel and request headers (called by the heartbeat task)
     */
    public void updateChannel(ManagedChannel channel, Map<String, String> headers) {
        ManagedChannel oldChannel = this.channel;
        this.channel = channel;
        this.headers = headers;
        
        log.info("gRPC channel updated, state: {}", 
            channel != null ? channel.getState(false) : "null");
        
        // Gracefully close old channels
        if (oldChannel != null && oldChannel != channel && !oldChannel.isShutdown()) {
            try {
                oldChannel.shutdown();
                log.debug("Old channel shutdown");
            } catch (Exception e) {
                log.warn("Failed to shutdown old channel", e);
            }
        }
    }
    
    /**
     * Get currently available channels
     * 
     * @throws CallbackChannelUnavailableException if channel is not available
     */
    public ManagedChannel getChannel() {
        ManagedChannel ch = this.channel;
        if (ch == null || ch.isShutdown() || ch.isTerminated()) {
            throw new CallbackChannelUnavailableException("gRPC channel is unavailable");
        }
        return ch;
    }

}

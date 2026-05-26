package com.aizuda.anik.ai.agent.common.exception;

/**
 * gRPC Channel is not available abnormal (can be retried)
 *
 * @author openanik
 * @date 2025-04-12
 */
public class CallbackChannelUnavailableException extends CallbackException {
    
    public CallbackChannelUnavailableException(String message) {
        super(message);
    }
}

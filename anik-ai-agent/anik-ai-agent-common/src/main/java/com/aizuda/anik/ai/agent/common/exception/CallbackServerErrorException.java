package com.aizuda.anik.ai.agent.common.exception;

/**
 * Abnormal server-side business error (cannot be retried)
 *
 * @author openanik
 * @date 2025-04-12
 */
public class CallbackServerErrorException extends CallbackException {
    
    public CallbackServerErrorException(String message) {
        super(message);
    }
}

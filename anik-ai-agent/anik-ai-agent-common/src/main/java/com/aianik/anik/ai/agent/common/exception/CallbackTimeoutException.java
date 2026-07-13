package com.aianik.anik.ai.agent.common.exception;

/**
 * Abnormal callback timeout (can be retried)
 *
 * @author openanik
 * @date 2025-04-12
 */
public class CallbackTimeoutException extends CallbackException {
    
    public CallbackTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.aianik.anik.ai.agent.common.exception;

import com.aianik.anik.ai.common.execption.AnikAiException;

/**
 * Server callback basics abnormal
 *
 * @author openanik
 * @date 2025-04-12
 */
public class CallbackException extends AnikAiException {
    
    public CallbackException(String message) {
        super(message);
    }
    
    public CallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}

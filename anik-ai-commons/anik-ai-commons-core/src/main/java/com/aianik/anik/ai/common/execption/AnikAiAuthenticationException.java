package com.aianik.anik.ai.common.execption;

import lombok.Getter;

/**
 * (Copied from anik-job)
 */
@Getter
public class AnikAiAuthenticationException extends BaseAnikAiException {
    private final Integer errorCode = 5001;

    public AnikAiAuthenticationException(final String message) {
        super(message);
    }

    public AnikAiAuthenticationException(String message, Object... arguments) {
        super(message, arguments);
    }

    public AnikAiAuthenticationException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public AnikAiAuthenticationException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public AnikAiAuthenticationException(String message, Object argument) {
        super(message, argument);
    }
}

package com.aianik.anik.ai.common.execption;

/**
 * Exception info
 */
public class AnikAiCommonException extends BaseAnikAiException {

    public AnikAiCommonException(String message) {
        super(message);
    }

    public AnikAiCommonException(String message, Object... arguments) {
        super(message, arguments);
    }

    public AnikAiCommonException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public AnikAiCommonException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public AnikAiCommonException(String message, Object argument) {
        super(message, argument);
    }
}

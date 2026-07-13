package com.aianik.anik.ai.common.execption;

/**
 * (Copied from anik-job-ai-executor)
 */
public class AnikAiException extends BaseAnikAiException {

    public AnikAiException(String message) {
        super(message);
    }

    public AnikAiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnikAiException(Throwable cause) {
        super(cause);
    }

    public AnikAiException(String message, Object... arguments) {
        super(message, arguments);
    }

    public AnikAiException(String message, Object[] arguments, Throwable cause) {
        super(message, arguments, cause);
    }

    public AnikAiException(String message, Object argument, Throwable cause) {
        super(message, argument, cause);
    }

    public AnikAiException(String message, Object argument) {
        super(message, argument);
    }
}

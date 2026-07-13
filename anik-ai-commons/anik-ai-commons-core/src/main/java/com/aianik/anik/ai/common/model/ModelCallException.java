package com.aianik.anik.ai.common.model;

/**
 * Dynamic model call exception
 */
public class ModelCallException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public ModelCallException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public ModelCallException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""));
        this.errorCode = errorCode;
        this.details = details;
    }

    public ModelCallException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = cause.getMessage();
    }

    public ModelCallException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage() + (details != null ? ": " + details : ""), cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }

    /**
     * Error code enum
     */
    public enum ErrorCode {
        CONFIG_NOT_FOUND("Configuration does not exist"),
        CONFIG_DISABLED("Configuration is disabled"),
        ACCESS_DENIED("Don't have permission to use this configuration"),
        PROVIDER_NOT_SUPPORTED("The provider does not support it yet"),
        CHAT_MODEL_BUILD_FAILED("ChatModel build failed"),
        CHAT_CLIENT_BUILD_FAILED("ChatClient build failed"),
        API_KEY_DECRYPT_FAILED("APIKey decryption failed"),
        MODEL_CALL_FAILED("Model call failed"),
        INVALID_PARAMETER("Illegal parameter"),
        CONFIG_PARSE_ERROR("Configuration parsing error");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}

package com.aianik.anik.ai.vector.storage.exception;

import com.aianik.anik.ai.common.execption.BaseAnikAiException;

public class VectorStoreException extends BaseAnikAiException {

    public VectorStoreException(String message) {
        super(message);
    }

    public VectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.aizuda.anik.ai.vector.storage.exception;

import com.aizuda.anik.ai.common.execption.BaseAnikAiException;

public class VectorStoreException extends BaseAnikAiException {

    public VectorStoreException(String message) {
        super(message);
    }

    public VectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}

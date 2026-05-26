package com.aizuda.anik.ai.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result<T> {

    protected int status = 1;

    protected String message;

    protected T data;

    public Result() {
    }

    public Result(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Result(T data) {
        this.data = data;
    }

    public Result(String message, T data) {
        this.data = data;
        this.message = message;
    }

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Successful response (with data)
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(1, "success", data);
    }

    /**
     * Successful response (with message and data)
     */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(1, message, data);
    }

    /**
     * Failure response
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(0, message, null);
    }

    /**
     * Failure response (with status code)
     */
    public static <T> Result<T> fail(int status, String message) {
        return new Result<>(status, message, null);
    }

}

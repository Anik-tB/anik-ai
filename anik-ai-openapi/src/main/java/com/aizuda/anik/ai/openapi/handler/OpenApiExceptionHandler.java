package com.aizuda.anik.ai.openapi.handler;

import com.aizuda.anik.ai.common.execption.BaseAnikAiException;
import com.aizuda.anik.ai.common.execption.AnikAiAuthenticationException;
import com.aizuda.anik.ai.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * OpenAPI unified abnormal processing
 *
 * @author openanik
 * @date 2026-04-24
 */
@Slf4j
@ControllerAdvice(basePackages = {"com.aizuda.anik.ai.openapi"})
@ResponseBody
public class OpenApiExceptionHandler {

    @ExceptionHandler(AnikAiAuthenticationException.class)
    public Result<Void> onAuthException(AnikAiAuthenticationException ex) {
        log.warn("OpenAPI auth failed: {}", ex.getMessage());
        return Result.fail(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(BaseAnikAiException.class)
    public Result<Void> onBusinessException(BaseAnikAiException ex) {
        log.error("OpenAPI business exception", ex);
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> onValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Parameter verification failed");
        return Result.fail(message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> onBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .orElse("Parameter verification failed");
        return Result.fail(message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> onException(Exception ex) {
        log.error("OpenAPI unexpected exception", ex);
        return Result.fail("System exception");
    }
}

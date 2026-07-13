package com.aianik.anik.ai.admin.handler;

import cn.hutool.core.collection.CollUtil;
import com.aianik.anik.ai.common.execption.AbstractError;
import com.aianik.anik.ai.common.execption.BaseAnikAiException;
import com.aianik.anik.ai.common.execption.AnikAiAuthenticationException;
import com.aianik.anik.ai.common.model.Result;
import com.aianik.anik.ai.common.util.StreamUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @description: 400 unified abnormal processing
 * @author: byteblogs
 * @date: 2019/09/30 17:02
 */
@ControllerAdvice(basePackages = {"com.aianik.anik.ai.admin"})
@Slf4j
@ResponseBody
public class RestExceptionHandler {
    //abnormal type
    public static final String DELIMITER_TO = "@";
    public static final String DELIMITER_COLON = ":";

    /**
     * businessabnormal
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({Exception.class})
    public Result onException(Exception ex) {
        log.error("Exception class onException,", ex);
        return new Result<String>(0, "System exception");
    }

    /**
     * businessabnormal
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({BaseAnikAiException.class})
    public Result onBusinessException(BaseAnikAiException ex) {
        log.error("Exception class businessException", ex);
        if (ex instanceof final AnikAiAuthenticationException authenticationException) {
            return new Result<String>(authenticationException.getErrorCode(), ex.getMessage());
        }

        return new Result<String>(0, ex.getMessage());
    }

    /**
     * 400 error
     *
     * @param ex
     * @return
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public Result requestNotReadable(HttpMessageNotReadableException ex) {
        log.error("Exception class HttpMessageNotReadableException,", ex);
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * validation abnormal processing
     *
     * @param e ConstraintViolationException
     * @return HttpResult
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result onConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        if (CollUtil.isNotEmpty(constraintViolations)) {
            String errorMessage = StreamUtils.join(constraintViolations, ConstraintViolation::getMessage, ";");
            return new Result(0, errorMessage);
        }

        return new Result<String>(0, e.getMessage());
    }

    /**
     * validation abnormal processing
     *
     * @param e MethodArgumentNotValidException
     * @return HttpResult
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        if (result != null && result.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            List<FieldError> errors = result.getFieldErrors();
            if (CollUtil.isNotEmpty(errors)) {
                FieldError error = errors.get(0);
                String rejectedValue = Objects.toString(error.getRejectedValue(), "");
                String defMsg = error.getDefaultMessage();
                // Exclude annotation prompts on classes
                if (rejectedValue.contains(DELIMITER_TO)) {
                    //Determine the error field yourself
                    sb.append(defMsg);
                } else {
                    if (DELIMITER_COLON.contains(defMsg)) {
                        sb.append(error.getField()).append(" ").append(defMsg);
                    } else {
                        sb.append(error.getField()).append(" ").append(defMsg).append(":").append(rejectedValue);
                    }
                }
            } else {
                String msg = result.getAllErrors().get(0).getDefaultMessage();
                sb.append(msg);
            }

            return new Result<String>(0, sb.toString());
        }

        return null;
    }

    /**
     * Controller parameter check error
     *
     * @param e abnormal object
     * @return HttpResult
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public Result onHandlerMethodValidationException(HandlerMethodValidationException e) {
        Object[] detailMessageArguments = e.getDetailMessageArguments();
        if (detailMessageArguments != null && detailMessageArguments.length > 0) {
            return new Result<String>(0, detailMessageArguments[0].toString());
        }

        return new Result<>("Parameter validation failed");

    }

    /**
     * 400 error
     */
    @ExceptionHandler({TypeMismatchException.class})
    public Result requestTypeMismatch(TypeMismatchException ex) {
        log.error("Exception class TypeMismatchException {},", ex.getMessage());
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * 400 error
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    public Result requestMissingServletRequest(MissingServletRequestParameterException ex) {
        log.error("Exception class MissingServletRequestParameterException {},", ex.getMessage());
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * 405 error
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    public Result request405() {
        log.error(" Exception class HttpRequestMethodNotSupportedException");
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }

    /**
     * 415 error
     */
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public Result request415(HttpMediaTypeNotSupportedException ex) {
        log.error("Exception class HttpMediaTypeNotSupportedException {}", ex.getMessage());
        return new Result<String>(0, AbstractError.PARAM_INCORRECT.toString());
    }
}

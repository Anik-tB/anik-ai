package com.aizuda.anik.ai.agent.common.rpc;

import com.aizuda.anik.ai.agent.common.rpc.annotation.Mapping;
import com.aizuda.anik.ai.agent.common.rpc.annotation.Param;
import com.aizuda.anik.ai.agent.common.config.AnikAiAgentProperties;
import com.aizuda.anik.ai.agent.common.exception.CallbackChannelUnavailableException;
import com.aizuda.anik.ai.agent.common.exception.CallbackException;
import com.aizuda.anik.ai.agent.common.exception.CallbackServerErrorException;
import com.aizuda.anik.ai.agent.common.exception.CallbackTimeoutException;
import com.aizuda.anik.ai.common.grpc.auto.GrpcAnikAiResult;
import com.aizuda.anik.ai.common.grpc.client.GrpcChannelUtil;
import com.aizuda.anik.ai.common.util.JsonUtil;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ServerCallback dynamic proxy core processor
 * <p>
 * Unified processing of timeout control, retry logic, and monitoring indicators
 *
 * @author openanik
 * @date 2025-04-12
 */
@Slf4j
@RequiredArgsConstructor
public class GrpcClientInvokeHandler implements InvocationHandler {

    private final GrpcChannelProvider channelProvider;
    private final AnikAiAgentProperties properties;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //Get @Mapping annotation
        Mapping mapping = method.getAnnotation(Mapping.class);
        if (mapping == null) {
            throw new UnsupportedOperationException("Method must have @Mapping annotation: " + method.getName());
        }

        String callbackName = method.getName();
        String uri = mapping.path();
        long timeout = mapping.timeout();

        // Execute calls (with retries and monitoring)
        return executeWithRetry(callbackName, uri, method, timeout, args, method.getReturnType());
    }

    /**
     * Execution calls (integrated retries and monitoring)
     */
    private Object executeWithRetry(String callbackName, String uri, Method method,
                                    long timeout, Object[] args, Class<?> returnType) {

        AnikAiAgentProperties.ServerConfig server = properties.getServer();
        int maxAttempts = server.getRetryTimes();
        int retryInterval = server.getRetryInterval();

        Throwable lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Object result = doInvoke(uri, method, timeout, args, returnType);
                if (attempt > 1) {
                    log.info("Callback succeeded after {} attempts: {}", attempt, callbackName);
                }

                return result;

            } catch (CallbackChannelUnavailableException | StatusRuntimeException | CallbackTimeoutException e) {
                lastException = e;

                if (attempt < maxAttempts) {
                    log.warn("Callback attempt #{} failed: {}, retrying in {}ms",
                            attempt, callbackName, retryInterval);

                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (CallbackServerErrorException e) {
                throw e;
            }
        }

        log.error("Callback failed after {} attempts: {}", maxAttempts, callbackName);
        throw new CallbackException("Callback failed after " + maxAttempts + " attempts: " + callbackName,
                lastException);
    }

    /**
     * Perform a single gRPC call
     */
    private Object doInvoke(String uri, Method method, long timeout, Object[] args, Class<?> returnType) {
        //1. Get channel (throw abnormal if failed)
        ManagedChannel channel = channelProvider.getChannel();

        //2. Build request parameters
        Map<String, Object> params = buildParams(method, args);

        if (timeout <= 0) {
            timeout = properties.getServer().getTimeout();
        }

        // 3. Add timeout control
        CallOptions options = CallOptions.DEFAULT.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS);

        // 4. Send gRPC request
        GrpcAnikAiResult result;
        try {
            result = GrpcChannelUtil.sendUnary(
                    channel, uri, JsonUtil.toJsonString(params),
                    channelProvider.getHeaders(), options
            );
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                throw new CallbackTimeoutException("Request timeout: " + uri, e);
            }
            throw e;
        }

        // 5. Check response status
        if (result == null || result.getStatus() != 1) {
            String errorMsg = result != null ? result.getMessage() : "null response";
            log.warn("Server callback error: uri={}, error={}", uri, errorMsg);
            throw new CallbackServerErrorException("Server error: " + errorMsg);
        }

        // 6. Parse the return value
        if (returnType == void.class || returnType == Void.class) {
            return null;
        }

        String data = result.getData();
        if (data.isEmpty() || "null".equals(data)) {
            return null;
        }

        return JsonUtil.parseObject(data, returnType);
    }

    /**
     * Build parameter map
     * <p>
     * Two modes are supported:
     * 1. DTO object: directly converted to Map (recommended)
     * 2. @Param annotation: Build Map according to annotation value
     */
    private Map<String, Object> buildParams(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return Map.of();
        }

        //If there is only one parameter and it is not a basic type, it is regarded as a DTO object.
        if (args.length == 1 && args[0] != null) {
            Object arg = args[0];

            // If it is a Map, return directly
            if (arg instanceof Map) {
                return (Map<String, Object>) arg;
            }

            // If it is a custom object (Request DTO), convert it to Map
            Class<?> argClass = arg.getClass();
            if (!isSimpleType(argClass)) {
                // Use JsonUtil to convert DTO to Map (preserve field names)
                String json = JsonUtil.toJsonString(arg);
                return JsonUtil.parseObject(json, Map.class);
            }
        }

        //Bottom line: Use the @Param annotation to build a parameter map
        Map<String, Object> params = new LinkedHashMap<>();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (args[i] == null) {
                continue;
            }

            Param paramAnnotation = parameters[i].getAnnotation(Param.class);
            if (paramAnnotation != null) {
                params.put(paramAnnotation.value(), args[i]);
            } else {
                String paramName = parameters[i].getName();
                params.put(paramName, args[i]);
            }
        }

        return params;
    }

    /**
     * Determine whether it is a simple type (basic type and its wrapper class, String)
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == String.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Double.class
                || clazz == Float.class
                || clazz == Boolean.class
                || clazz == Short.class
                || clazz == Byte.class
                || clazz == Character.class;
    }
}

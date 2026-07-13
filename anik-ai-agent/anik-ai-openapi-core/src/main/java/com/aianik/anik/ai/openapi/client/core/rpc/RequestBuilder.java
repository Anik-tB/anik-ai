package com.aianik.anik.ai.openapi.client.core.rpc;

import com.aianik.anik.ai.agent.common.config.AnikAiAgentProperties;
import com.aianik.anik.ai.openapi.client.core.config.AnikAiOpenApiProperties;

import java.lang.reflect.Proxy;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * client proxy factory
 *
 * @author openanik
 * @date 2026-04-24
 */
public final class RequestBuilder {

    private RequestBuilder() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> iface, HttpClient httpClient,
                                    AnikAiOpenApiProperties openApiProperties,
                                    AnikAiAgentProperties  aiAgentProperties) {
        OpenApiHttpInvokeHandler handler = new OpenApiHttpInvokeHandler(httpClient, openApiProperties, aiAgentProperties);
        return (T) Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class<?>[]{iface},
                handler);
    }

    public static HttpClient buildHttpClient(AnikAiOpenApiProperties properties) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
    }
}

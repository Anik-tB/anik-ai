package com.aianik.anik.ai.openapi.config;

import com.aianik.anik.ai.openapi.interceptor.OpenApiAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * OpenAPI WebMvc configuration, register authentication interceptor to /openapi/** path
 *
 * @author openanik
 * @date 2026-04-24
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiWebMvcConfiguration implements WebMvcConfigurer {

    private final OpenApiAuthInterceptor openApiAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(openApiAuthInterceptor)
                .addPathPatterns("/openapi/**");
    }
}

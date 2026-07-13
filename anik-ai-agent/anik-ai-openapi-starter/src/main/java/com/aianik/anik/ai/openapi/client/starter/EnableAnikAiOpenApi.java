package com.aianik.anik.ai.openapi.client.starter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * enable Anik AI OpenAPI client
 *
 * @author openanik
 * @date 2026-04-24
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AnikAiOpenApiAutoConfiguration.class)
public @interface EnableAnikAiOpenApi {
}

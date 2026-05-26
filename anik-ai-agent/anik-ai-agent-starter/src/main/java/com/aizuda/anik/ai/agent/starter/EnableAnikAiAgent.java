package com.aizuda.anik.ai.agent.starter;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AnikAiAgentAutoConfiguration.class)
public @interface EnableAnikAiAgent {
}

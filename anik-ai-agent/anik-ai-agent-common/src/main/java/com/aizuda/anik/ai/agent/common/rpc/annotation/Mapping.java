package com.aizuda.anik.ai.agent.common.rpc.annotation;

import java.lang.annotation.*;

/**
 * Server callback method mapping annotations
 *
 * @author openanik
 * @date 2025-04-12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mapping {
    
    /**
     * callback path (e.g. /callback/skill/content)
     */
    String path();

    /**
     * Timeout (ms)
     *
     * @return 5000ms
     */
    long timeout() default 0;
}

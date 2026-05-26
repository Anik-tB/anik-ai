package com.aizuda.anik.ai.agent.common.rpc.annotation;

import java.lang.annotation.*;

/**
 * Callback method parameter annotation
 * Used to mark parameter names and support dynamic agents to automatically build parameter maps.
 *
 * @author openanik
 * @date 2025-04-12
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {
    
    /**
     * parameter name
     */
    String value();
}

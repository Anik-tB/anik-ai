package com.aizuda.anik.ai.common;

/**
 * Component life cycle
 *
 * @author: openanik
 * @date : 2021-11-19 14:43
 */
public interface Lifecycle {

    /**
     * Startup component
     */
    void start();

    /**
     * Close component
     */
    void close();

}

package com.aianik.anik.ai.agent.common.window;

import java.util.List;

/**
 * sliding window listener
 *
 * @author openanik
 */
@FunctionalInterface
public interface Listener<T> {

    /**
     * data listener handler
     *
     * @param list data arriving in the window period
     */
    void handler(List<T> list);
}

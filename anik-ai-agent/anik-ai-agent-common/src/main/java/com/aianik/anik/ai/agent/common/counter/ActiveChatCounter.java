package com.aianik.anik.ai.agent.common.counter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * active conversation counter (Agent instance level sharing)
 *
 * @author openanik
 * @date 2025-04-08
 */
public class ActiveChatCounter {

    private final AtomicInteger count = new AtomicInteger(0);

    public int increment() {
        return count.incrementAndGet();
    }

    public int decrement() {
        return count.decrementAndGet();
    }

    public int get() {
        return count.get();
    }
}

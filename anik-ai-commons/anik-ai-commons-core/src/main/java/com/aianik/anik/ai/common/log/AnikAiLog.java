package com.aianik.anik.ai.common.log;

import com.aianik.anik.ai.common.log.strategy.Local;
import com.aianik.anik.ai.common.log.strategy.Remote;

/**
 * Static logger class (copied from anik-job, uses simplified Local/Remote)
 */
public final class AnikAiLog {
    private AnikAiLog() {
    }

    public static final Local LOCAL = new Local();
    public static final Remote REMOTE = new Remote();
}

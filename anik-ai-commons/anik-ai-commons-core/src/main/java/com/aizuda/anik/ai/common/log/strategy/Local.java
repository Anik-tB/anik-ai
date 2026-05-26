package com.aizuda.anik.ai.common.log.strategy;

/**
 * (Copied from anik-job)
 */
public final class Local extends AbstractLog {
    public Local() {
        setRemote(Boolean.FALSE);
    }
}

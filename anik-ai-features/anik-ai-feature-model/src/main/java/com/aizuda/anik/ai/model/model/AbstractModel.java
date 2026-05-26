package com.aizuda.anik.ai.model.model;

import com.aizuda.anik.ai.model.dto.ModelConfigInfoDTO;

/**
 * author: openanik
 * date: 2026-03-04
 */
public abstract class AbstractModel implements Model {
    protected ModelConfigInfoDTO modelConfigInfo;

    @Override
    public void setModelConfigInfo(ModelConfigInfoDTO modelConfigInfo) {
        this.modelConfigInfo = modelConfigInfo;
    }
}

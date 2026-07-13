package com.aianik.anik.ai.model.model;

import com.aianik.anik.ai.model.dto.ModelConfigInfoDTO;

/**
 * author: openanik
 * date: 2026-03-04
 */
public interface Model {

    boolean supports(String modelKey);

    void setModelConfigInfo(ModelConfigInfoDTO modelConfigInfo);
}

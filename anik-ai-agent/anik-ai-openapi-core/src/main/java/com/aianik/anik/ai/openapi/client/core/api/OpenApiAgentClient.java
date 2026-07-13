package com.aianik.anik.ai.openapi.client.core.api;

import com.aianik.anik.ai.common.constants.OpenApiPathConstants;
import com.aianik.anik.ai.openapi.client.core.annotation.OpenApiMapping;
import com.aianik.anik.ai.common.openapi.dto.OpenApiAgentVO;
import com.aianik.anik.ai.common.openapi.dto.OpenApiAgentIdentityRequest;
import com.aianik.anik.ai.common.model.Result;

import java.util.List;

/**
 * OpenAPI Agent queries the client interface
 *
 * @author openanik
 * @date 2026-04-24
 */
public interface OpenApiAgentClient {

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENTS, method = OpenApiMapping.HttpMethod.GET)
    Result<List<OpenApiAgentVO>> listAgents();

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT, method = OpenApiMapping.HttpMethod.GET)
    Result<OpenApiAgentVO> getAgent(OpenApiAgentIdentityRequest request);
}

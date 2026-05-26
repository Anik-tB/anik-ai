package com.aizuda.anik.ai.openapi.client.core.api;

import com.aizuda.anik.ai.common.constants.OpenApiPathConstants;
import com.aizuda.anik.ai.openapi.client.core.annotation.OpenApiMapping;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiChatRequest;
import com.aizuda.anik.ai.common.openapi.dto.OpenApiChatSyncResponse;
import com.aizuda.anik.ai.openapi.client.core.listener.SseEventListener;
import com.aizuda.anik.ai.common.model.Result;

/**
 * OpenAPI Chat client interface
 *
 * @author openanik
 * @date 2026-04-24
 */
public interface OpenApiChatClient {

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT_CHAT, method = OpenApiMapping.HttpMethod.POST)
    void chatStream(OpenApiChatRequest request,
                    SseEventListener listener);

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT_CHAT_SYNC, method = OpenApiMapping.HttpMethod.POST)
    Result<OpenApiChatSyncResponse> chatSync(OpenApiChatRequest request);
}

package com.aianik.anik.ai.openapi.client.core.api;

import com.aianik.anik.ai.common.constants.OpenApiPathConstants;
import com.aianik.anik.ai.common.model.PageResult;
import com.aianik.anik.ai.common.model.Result;
import com.aianik.anik.ai.common.openapi.dto.OpenApiConversationIdentityRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiConversationQueryRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiConversationVO;
import com.aianik.anik.ai.openapi.client.core.annotation.OpenApiMapping;
import com.aianik.anik.ai.common.openapi.dto.OpenApiCreateConversationRequest;
import com.aianik.anik.ai.common.openapi.dto.OpenApiMessageVO;

import java.util.List;

/**
 * OpenAPI session management client interface
 *
 * @author openanik
 * @date 2026-04-24
 */
public interface OpenApiConversationClient {

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT_CONVERSATIONS, method = OpenApiMapping.HttpMethod.GET)
    PageResult<List<OpenApiConversationVO>> listConversations(OpenApiConversationQueryRequest request);

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT_CONVERSATIONS, method = OpenApiMapping.HttpMethod.POST)
    Result<OpenApiConversationVO> createConversation(OpenApiCreateConversationRequest request);

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT_CONVERSATIONS, method = OpenApiMapping.HttpMethod.DELETE)
    Result<Void> deleteConversation(OpenApiConversationIdentityRequest request);

    @OpenApiMapping(path = OpenApiPathConstants.OPEN_API_AGENT_CONVERSATION_MESSAGES, method = OpenApiMapping.HttpMethod.GET)
    Result<List<OpenApiMessageVO>> getMessages(OpenApiConversationIdentityRequest request);
}

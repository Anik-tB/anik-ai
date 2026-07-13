package com.aianik.anik.ai.openapi.client.starter;

import com.aianik.anik.ai.agent.common.config.AnikAiAgentProperties;
import com.aianik.anik.ai.openapi.client.core.api.OpenApiAgentClient;
import com.aianik.anik.ai.openapi.client.core.api.OpenApiChatClient;
import com.aianik.anik.ai.openapi.client.core.api.OpenApiConversationClient;
import com.aianik.anik.ai.openapi.client.core.api.OpenApiUserClient;
import com.aianik.anik.ai.openapi.client.core.AnikAiOpenApi;
import com.aianik.anik.ai.openapi.client.core.config.AnikAiOpenApiProperties;
import com.aianik.anik.ai.openapi.client.core.rpc.RequestBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class AnikAiOpenApiAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "anik-ai.openapi")
    public AnikAiOpenApiProperties anikAiOpenApiProperties() {
        return new AnikAiOpenApiProperties();
    }

    @Bean
    public HttpClient anikAiOpenApiHttpClient(AnikAiOpenApiProperties properties) {
        return RequestBuilder.buildHttpClient(properties);
    }

    @Bean
    public OpenApiChatClient openApiChatClient(HttpClient anikAiOpenApiHttpClient,
                                               AnikAiOpenApiProperties openApiProperties,
                                               AnikAiAgentProperties aiAgentProperties) {
        return RequestBuilder.createProxy(OpenApiChatClient.class, anikAiOpenApiHttpClient, openApiProperties, aiAgentProperties);
    }

    @Bean
    public OpenApiConversationClient openApiConversationClient(HttpClient anikAiOpenApiHttpClient,
                                                               AnikAiOpenApiProperties properties,
                                                               AnikAiAgentProperties aiAgentProperties) {
        return RequestBuilder.createProxy(OpenApiConversationClient.class, anikAiOpenApiHttpClient, properties, aiAgentProperties);
    }

    @Bean
    public OpenApiAgentClient openApiAgentClient(HttpClient anikAiOpenApiHttpClient,
                                                 AnikAiOpenApiProperties properties,
                                                 AnikAiAgentProperties aiAgentProperties) {
        return RequestBuilder.createProxy(OpenApiAgentClient.class, anikAiOpenApiHttpClient, properties, aiAgentProperties);
    }

    @Bean
    public OpenApiUserClient openApiUserClient(HttpClient anikAiOpenApiHttpClient,
                                               AnikAiOpenApiProperties properties,
                                               AnikAiAgentProperties aiAgentProperties) {
        return RequestBuilder.createProxy(OpenApiUserClient.class, anikAiOpenApiHttpClient, properties, aiAgentProperties);
    }

    @Bean
    public AnikAiOpenApi anikAiOpenApi(OpenApiChatClient chatClient,
                                         OpenApiConversationClient conversationClient,
                                         OpenApiAgentClient agentClient,
                                         OpenApiUserClient userClient) {
        AnikAiOpenApi.init(chatClient, conversationClient, agentClient, userClient);
        return new AnikAiOpenApi();
    }
}

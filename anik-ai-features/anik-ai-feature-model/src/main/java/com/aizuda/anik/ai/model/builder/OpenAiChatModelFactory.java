package com.aizuda.anik.ai.model.builder;

import com.aizuda.anik.ai.common.model.ConfigExtAttrsDTO;
import com.aizuda.anik.ai.common.model.ModelCallException;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * OpenAI ChatModel factory implementation
 * Support OpenAI and compatible interfaces (Huoshan Cloud, Tongyi Qianwen, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiChatModelFactory implements ChatModelFactory {
    private final ObservationRegistry observationRegistry;

    @Override
    public String getSupportedProvider() {
        return "openai";
    }

    @Override
    public ChatModel createChatModel(String providerKey, String baseUrl, String apiKey,
                                     String modelKey, ConfigExtAttrsDTO configExtAttrsDTO) throws Exception {
        try {
            // 1. Verify configuration
            if (!isConfigValid(baseUrl, apiKey)) {
                throw new ModelCallException(ModelCallException.ErrorCode.INVALID_PARAMETER,
                        "baseUrl and apiKey cannot be empty");
            }


            log.debug("Creating OpenAiChatModel for provider: {}, model: {}, baseUrl: {}",
                    providerKey, modelKey, baseUrl);
            // 2. Timeout time: The read timeout uses the configured timeoutMs. If not configured, the default is 60s; the connection timeout uses min(timeoutMs, 10s)
            Long timeoutMs = configExtAttrsDTO.getTimeoutMs();
            long readTimeoutMs = (timeoutMs != null && timeoutMs > 0) ? timeoutMs : 60_000L;
            long connectTimeoutMs = Math.min(readTimeoutMs, 10_000L);

            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
            requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

            RestClient.Builder restClientBuilder = RestClient.builder()
                    .requestFactory(requestFactory);

            // 3. Create OpenAiApi (RestClient with timeout)
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .completionsPath(configExtAttrsDTO.getCompletionsPath())
                    .restClientBuilder(restClientBuilder)
                    .build();

            // 3. Parse the configuration JSON and build OpenAiChatOptions
            OpenAiChatOptions chatOptions = buildChatOptions(modelKey, configExtAttrsDTO);

            //4. Create ChatModel (use the overall situation to share ObservationRegistry and configure it uniformly by ObservationConfig)
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(chatOptions)
                    .retryTemplate(RetryUtils.DEFAULT_RETRY_TEMPLATE)
                    .observationRegistry(observationRegistry)
                    .toolCallingManager(ToolCallingManager.builder().observationRegistry(observationRegistry).build())
                    .build();


            log.info("Successfully created OpenAiChatModel for model: {}", modelKey);
            return chatModel;

        } catch (ModelCallException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create OpenAiChatModel for model: {}", modelKey, e);
            throw new ModelCallException(
                    ModelCallException.ErrorCode.CHAT_MODEL_BUILD_FAILED,
                    "Unable to create ChatModel: " + e.getMessage(),
                    e
            );
        }
    }

    private OpenAiChatOptions buildChatOptions(String modelKey, ConfigExtAttrsDTO config) {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder().model(modelKey);
        if (config == null) {
            return builder.build();
        }
        if (config.getTemperature() != null) {
            builder.temperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            builder.maxTokens(config.getMaxTokens());
        }
        if (config.getTopP() != null) {
            builder.topP(config.getTopP());
        }
        if (config.getFrequencyPenalty() != null) {
            builder.frequencyPenalty(config.getFrequencyPenalty());
        }
        if (config.getPresencePenalty() != null) {
            builder.presencePenalty(config.getPresencePenalty());
        }
        if (config.getStopSequences() != null && !config.getStopSequences().isEmpty()) {
            builder.stop(config.getStopSequences());
        }
        if (config.getSeed() != null) {
            builder.seed(config.getSeed().intValue());
        }
        return builder.build();
    }
}

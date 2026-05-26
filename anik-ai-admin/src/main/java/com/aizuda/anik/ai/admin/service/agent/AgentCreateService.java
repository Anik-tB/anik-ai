package com.aizuda.anik.ai.admin.service.agent;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.common.enums.agent.AgentStatusEnum;
import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.model.builder.ChatClientBuilder;
import com.aizuda.anik.ai.admin.service.model.AiModelConfigService;
import com.aizuda.anik.ai.model.enums.ModelTypeEnum;
import com.aizuda.anik.ai.model.handle.ModelConfigHandler;
import com.aizuda.anik.ai.model.dto.ModelConfigInfoDTO;
import com.aizuda.anik.ai.persistence.agent.mapper.AgentMapper;
import com.aizuda.anik.ai.persistence.agent.po.AgentPO;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigVO;
import com.aizuda.anik.ai.persistence.security.UserSessionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentCreateService {

    private final AgentMapper agentMapper;
    private final AiModelConfigService aiModelConfigService;
    private final ModelConfigHandler modelConfigHandler;
    private final ChatClientBuilder chatClientBuilder;
    private final Executor anikAiAsyncExecutor;

    /**
     * Agent information record
     */
    private record AgentInfo(String name, String description, String greeting, List<String> presetQuestions, String instruction) {}

    /**
     * Streaming agent creation (single LLM call + phased push)
     */
    public void createByDescriptionStream(String description, ResponseBodyEmitter emitter) {
        Long userId = UserSessionUtils.currentUserSession().getId();
        CompletableFuture.runAsync(() -> doCreateStream(description, userId, emitter), anikAiAsyncExecutor);
    }

    private void doCreateStream(String description, Long userId, ResponseBodyEmitter emitter) {
        try {
            // 1. Send start signal
            emitter.send("[START]\n");

            // 2. Get the default CHAT model
            AiModelConfigVO defaultModel = aiModelConfigService.getDefaultModelByType(ModelTypeEnum.CHAT.getValue());
            if (defaultModel == null) {
                emitter.send("[ERROR]Default CHAT model not found\n");
                emitter.completeWithError(new AnikAiException("Default CHAT model not found"));
                return;
            }

            // 3. Build ChatClient
            ChatClient chatClient;
            try {
                ModelConfigInfoDTO configInfo = modelConfigHandler.getConfigInfo(defaultModel.getId());
                String apiKey = modelConfigHandler.decryptApiKey(configInfo.getEncryptedApiKey());
                chatClient = chatClientBuilder.getOrBuildChatClient(apiKey, configInfo);
            } catch (Exception e) {
                log.error("Building ChatClient failed", e);
                emitter.send("[ERROR]Failed to build ChatClient\n");
                emitter.completeWithError(e);
                return;
            }

            // 4. Build structured prompts
            String structuredPrompt = buildStructuredPrompt(description);

            //5. Accumulation buffer and field push marking
            AtomicReference<StringBuilder> buffer = new AtomicReference<>(new StringBuilder());
            Set<String> pushedFields = java.util.concurrent.ConcurrentHashMap.newKeySet();

            // 6. Streaming call to LLM
            chatClient.prompt()
                    .user(structuredPrompt)
                    .stream()
                    .chatResponse()
                    .subscribe(
                            chatResponse -> {
                                // onNext: Process each chunk
                                String text = java.util.Optional.ofNullable(chatResponse)
                                        .map(org.springframework.ai.chat.model.ChatResponse::getResult)
                                        .map(org.springframework.ai.chat.model.Generation::getOutput)
                                        .map(org.springframework.ai.chat.messages.AssistantMessage::getText)
                                        .orElse(null);

                                if (org.springframework.util.StringUtils.hasText(text)) {
                                    buffer.get().append(text);

                                    // Try to detect field completion
                                    tryDetectAndPushFields(buffer.get().toString(), pushedFields, emitter);
                                }
                            },
                            error -> {
                                // onError: error handling
                                log.error("Streaming creation failed", error);
                                try {
                                    emitter.send("[ERROR]" + error.getMessage() + "\n");
                                    emitter.completeWithError(error);
                                } catch (IOException ignored) {
                                }
                            },
                            () -> {
                                // onComplete: The stream ends, only complete data is pushed, and the table is not dropped.
                                try {
                                    String jsonContent = extractJson(buffer.get().toString());
                                    AgentInfo agentInfo = parseAgentInfo(jsonContent);

                                    // Make sure all fields are pushed (to prevent missed push)
                                    ensureAllFieldsPushed(agentInfo, pushedFields, emitter);

                                    //The build completes but does not save to the database. The user will not be logged out until he confirms by clicking on the confirmation page.
                                    emitter.send("[DONE]\n");
                                    emitter.complete();
                                } catch (Exception e) {
                                    log.error("Failed to generate content", e);
                                    try {
                                        emitter.send("[ERROR]" + e.getMessage() + "\n");
                                        emitter.completeWithError(e);
                                    } catch (IOException ignored) {
                                    }
                                }
                            }
                    );

        } catch (Exception e) {
            log.error("Initial streaming creation failed", e);
            try {
                emitter.send("[ERROR]" + e.getMessage() + "\n");
                emitter.completeWithError(e);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Build structured prompts
     */
    private String buildStructuredPrompt(String userDescription) {
        return String.format("""
                You are an expert in AI assistant design. Please design an intelligent assistant based on the user's needs description.
                
                User requirements: %s
                
                Please output strictly in the following JSON format and do not include any other text description:
                
                {
                  "name": "The name of the smart assistant (short and attractive, 5-10 Chinese characters)",
                  "description": "Detailed description (100-200 words, describing functions, applicable scenarios, and features)",
                  "greeting": "Welcome message (friendly, 30-50 words, concise self-introduction)",
                  "presetQuestions": [
                    "Recommended question 1 (specific and practical example questions)",
                    "Recommended question 2",
                    "Recommended question 3"
                  ],
                  "instruction": "system instructions (detailed role setting and behavior guide, 300-500 words, including: role positioning, professional fields, answer style, precautions, etc.)"
                }
                
                Require:
                1. All content must be in Chinese
                2. The name should be creative and easy to remember.
                3. The description should highlight core values.
                4. The welcome message is only a concise greeting and self-introduction, and does not include a list of questions.
                5. presetQuestions are 3-5 recommended questions, used to guide users to start a conversation
                6. Instructions must be detailed and specific to effectively guide AI behavior.
                7. Strictly adhere to the JSON format to ensure parsability
                
                Now please output JSON directly:
                """, userDescription);
    }

    /**
     * Parse JSON response
     */
    private AgentInfo parseAgentInfo(String jsonResponse) {
        try {
            // 1. Clean up possible Markdown code block tags
            String cleanJson = jsonResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            //2. Find the start and end positions of the JSON object
            int startIndex = cleanJson.indexOf('{');
            int endIndex = cleanJson.lastIndexOf('}');

            if (startIndex == -1 || endIndex == -1) {
                throw new AnikAiException("No valid JSON object found in response");
            }

            String jsonContent = cleanJson.substring(startIndex, endIndex + 1);

            // 3. Use Jackson to parse
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonContent);

            // Parse preset question array
            List<String> presetQuestions = new ArrayList<>();
            JsonNode questionsNode = root.get("presetQuestions");
            if (questionsNode != null && questionsNode.isArray()) {
                for (JsonNode questionNode : questionsNode) {
                    String question = questionNode.asText().trim();
                    if (StrUtil.isNotBlank(question)) {
                        presetQuestions.add(question);
                    }
                }
            }

            return new AgentInfo(
                    root.get("name").asText(),
                    root.get("description").asText(),
                    root.get("greeting").asText(),
                    presetQuestions,
                    root.get("instruction").asText()
            );
        } catch (Exception e) {
            log.error("Failed to parse JSON, original response: {}", jsonResponse, e);
            throw new AnikAiException("Failed to parse agent information:" + e.getMessage(), e);
        }
    }

    /**
     * Try to detect and push completed fields
     */
    private void tryDetectAndPushFields(String content, Set<String> pushedFields, ResponseBodyEmitter emitter) {
        try {
            // Field order: name -> description -> greeting -> presetQuestions -> instruction
            String[] stringFields = {"name", "description", "greeting", "instruction"};

            // Detect string type fields
            for (String field : stringFields) {
                if (pushedFields.contains(field)) {
                    continue; //Already pushed, skip
                }

                // Use regex to detect complete fields (including quotes and commas/closed brackets)
                //Matches: "fieldName": "value", or "fieldName": "value"}
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "\"" + field + "\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*[,}]",
                        java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.MULTILINE
                );

                java.util.regex.Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String value = matcher.group(1);

                    // Field completed, push immediately
                    emitter.send("[FIELD_DONE]" + field + ":" + value + "\n");
                    pushedFields.add(field);

                    log.info("Field {} completed and pushed", field);
                }
            }

            // Detecting presetQuestions array fields
            if (!pushedFields.contains("presetQuestions")) {
                // Matches "presetQuestions": [...] format (array may span multiple lines)
                java.util.regex.Pattern arrayPattern = java.util.regex.Pattern.compile(
                        "\"presetQuestions\"\\s*:\\s*\\[([^\\]]*)]",
                        java.util.regex.Pattern.DOTALL | java.util.regex.Pattern.MULTILINE
                );

                java.util.regex.Matcher arrayMatcher = arrayPattern.matcher(content);
                if (arrayMatcher.find()) {
                    // Use capture groups to spell out the complete array JSON and compress it into a single line (to avoid line breaks being truncated by the front-end split)
                    String arrayJson = "[" + arrayMatcher.group(1).replaceAll("\\s*\\n\\s*", " ").trim() + "]";

                    // Field completion, push complete JSON array
                    emitter.send("[FIELD_DONE]presetQuestions:" + arrayJson + "\n");
                    pushedFields.add("presetQuestions");

                    log.info("Field presetQuestions completed and pushed");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to detect fields", e);
        }
    }

    /**
     * Extract pure JSON content (remove Markdown wrapping)
     */
    private String extractJson(String content) {
        String clean = content
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        int start = clean.indexOf('{');
        int end = clean.lastIndexOf('}');

        if (start == -1 || end == -1) {
            throw new AnikAiException("No valid JSON found");
        }

        return clean.substring(start, end + 1);
    }

    /**
     * Make sure all fields have been pushed (to prevent missing pushes)
     */
    private void ensureAllFieldsPushed(AgentInfo agentInfo, Set<String> pushedFields, ResponseBodyEmitter emitter) {
        try {
            if (!pushedFields.contains("name")) {
                emitter.send("[FIELD_DONE]name:" + agentInfo.name() + "\n");
            }
            if (!pushedFields.contains("description")) {
                emitter.send("[FIELD_DONE]description:" + agentInfo.description() + "\n");
            }
            if (!pushedFields.contains("greeting")) {
                emitter.send("[FIELD_DONE]greeting:" + agentInfo.greeting() + "\n");
            }
            if (!pushedFields.contains("presetQuestions") && !agentInfo.presetQuestions().isEmpty()) {
                String questionsJson = JsonUtil.toJsonString(agentInfo.presetQuestions());
                emitter.send("[FIELD_DONE]presetQuestions:" + questionsJson + "\n");
            }
            if (!pushedFields.contains("instruction")) {
                emitter.send("[FIELD_DONE]instruction:" + agentInfo.instruction() + "\n");
            }
        } catch (IOException e) {
            log.warn("Field push failed", e);
        }
    }
}

package com.aianik.anik.ai.features.rag.strategy.chunker;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.model.builder.ChatClientBuilder;
import com.aianik.anik.ai.features.rag.enums.ChunkModeEnum;
import com.aianik.anik.ai.model.enums.ModelTypeEnum;
import com.aianik.anik.ai.model.handle.ModelConfigHandler;
import com.aianik.anik.ai.model.dto.ModelConfigInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Smart slicing: LLM outputs semantic fragments as first-level segmentation, and then goes through {@link TokenAwareChunker#chunkParagraphs} second-level recursion.
 */
@Slf4j
@Component
public class SmartChunkStrategy extends AbstractChunkStrategy {

    // The maximum number of characters per paragraph is approximately equal to 4k tokens, ensuring stable LLM response.
    private static final int SEGMENT_CHARS = 10_000;
    //Maximum number of characters for a single LLM call (small documents are processed directly)
    private static final int MAX_LLM_INPUT_CHARS = 48_000;
    // Smart slicing outputs the maximum tokens to ensure enough to output a complete JSON array
    private static final int CHUNK_MAX_TOKENS = 16_384;

    private static final String SYSTEM = """
            You are the document slicing assistant. Please split the user-supplied text into complete fragments semantically.
            
            Require:
            1. Output a JSON array, each element is a complete semantic fragment
            2. Fragments should be complete and independently understandable
            3. Do not add any explanation or Markdown formatting
            4. Make sure to output a complete JSON array, ending with ]
            
            Output format:
            ["Fragment 1","Fragment 2","Fragment 3"]
            """;

    private final ModelConfigHandler modelConfigHandler;
    private final ChatClientBuilder chatClientBuilder;

    public SmartChunkStrategy(
            TokenAwareChunker chunker,
            ModelConfigHandler modelConfigHandler,
            ChatClientBuilder chatClientBuilder) {
        super(chunker);
        this.modelConfigHandler = modelConfigHandler;
        this.chatClientBuilder = chatClientBuilder;
    }

    @Override
    public boolean supports(ChunkModeEnum mode) {
        return ChunkModeEnum.SMART == mode;
    }

    @Override
    protected String[] splitIntoParagraphs(ChunkContext ctx) {
        Long modelId = ctx.getChunkModelId();
        if (modelId == null) {
            log.warn("smart mode but chunkModelId null, fallback to length");
            return new String[]{ctx.getContent()};
        }

        ModelConfigInfoDTO config = modelConfigHandler.getConfigInfo(modelId);
        if (config == null) {
            throw new IllegalArgumentException("Slice model does not exist: " + modelId);
        }
        if (!ModelTypeEnum.CHAT.getValue().equalsIgnoreCase(StrUtil.blankToDefault(config.getModelType(), ""))) {
            throw new IllegalArgumentException("Smart slicing must select the \"conversation\" type model");
        }
        String apiKey = modelConfigHandler.decryptApiKey(config.getEncryptedApiKey());
        if (StrUtil.isBlank(apiKey)) {
            throw new IllegalArgumentException("Model API Key is not available");
        }

        String content = ctx.getContent();
        ChatClient chatClient = chatClientBuilder.getOrBuildChatClient(apiKey, config);

        // Small documents are processed directly
        if (content.length() <= MAX_LLM_INPUT_CHARS) {
            return doSmartChunk(chatClient, content);
        }

        // Large document segmentation processing
        log.info("Intelligent slicing of large documents: document length {}, using segmentation processing", content.length());
        return doSegmentedSmartChunk(chatClient, content);
    }

    /**
     * Single smart slicing
     */
    private String[] doSmartChunk(ChatClient chatClient, String content) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM.trim()),
                new UserMessage("The full text is as follows:\n\n" + content)
        ), buildChunkOptions());

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String raw = response.getResult().getOutput().getText();
        
        if (StrUtil.isBlank(raw)) {
            log.warn("Smart slicing model returns empty, fallback to length mode");
            return new String[]{content};
        }
        
        List<String> segments = parseJsonStringArray(raw);
        if (segments.isEmpty()) {
            log.warn("Smart slicing is not parsed into JSON array, fallback");
            return new String[]{content};
        }
        return segments.toArray(new String[0]);
    }

    /**
     * Intelligent slicing of large document segments
     */
    private String[] doSegmentedSmartChunk(ChatClient chatClient, String content) {
        List<String> allSegments = new ArrayList<>();
        int totalLen = content.length();
        int segmentCount = (int) Math.ceil((double) totalLen / SEGMENT_CHARS);

        for (int i = 0; i < segmentCount; i++) {
            int start = i * SEGMENT_CHARS;
            int end = Math.min(start + SEGMENT_CHARS, totalLen);
            String segment = content.substring(start, end);

            log.info("Smart slicing segmentation {}/{}: character range {}-{}", i + 1, segmentCount, start, end);

            List<String> segments = callLLMForChunk(chatClient, segment, i + 1, segmentCount);
            if (segments.isEmpty()) {
                allSegments.add(segment);
            } else {
                allSegments.addAll(segments);
            }
        }

        return allSegments.toArray(new String[0]);
    }

    /**
     * Call LLM for slicing, with abnormal processing
     */
    private List<String> callLLMForChunk(ChatClient chatClient, String content, int segIndex, int totalSeg) {
        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(SYSTEM.trim()),
                    new UserMessage("Document No." + segIndex + "/" + totalSeg + "Part:\n\n" + content)
            ), buildChunkOptions());

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String raw = response.getResult().getOutput().getText();
            
            if (StrUtil.isBlank(raw)) {
                log.warn("Segment {} model returns empty, retain the original text", segIndex);
                return List.of();
            }

            List<String> segments = parseJsonStringArray(raw);
            if (segments.isEmpty()) {
                log.warn("Segment {} not parsed into JSON array, keep original text", segIndex);
                return List.of();
            }
            return segments;
        } catch (Exception e) {
            log.warn("Section {} Call model exception: {}, keep the original text", segIndex, e.getMessage());
            return List.of();
        }
    }

    /**
     * Build smart slicing-specific ChatOptions and set maxTokens large enough
     */
    private OpenAiChatOptions buildChunkOptions() {
        return OpenAiChatOptions.builder()
                .maxTokens(CHUNK_MAX_TOKENS)
                .build();
    }

    private List<String> parseJsonStringArray(String raw) {
        String s = raw.trim();
        
        // Remove Markdown code fences
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            int endFence = s.lastIndexOf("```");
            if (firstNl > 0 && endFence > firstNl) {
                s = s.substring(firstNl + 1, endFence).trim();
            }
        }
        // Extract JSON array part
        int start = s.indexOf('[');
        int end = s.lastIndexOf(']');
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1);
        }

        //Try standard JSON parsing
        try {
            var jsonNode = JsonUtil.toJson(s);
            if (jsonNode != null && jsonNode.isArray()) {
                List<String> result = new ArrayList<>();
                for (var node : jsonNode) {
                    String text = node.asText();
                    if (StrUtil.isNotBlank(text)) {
                        result.add(text.trim());
                    }
                }
                if (!result.isEmpty()) {
                    return result;
                }
            }
        } catch (Exception e) {
            log.debug("Standard JSON parsing failed, try regular extraction: {}", e.getMessage());
        }

        // Fallback: Use regular expressions to extract strings from JSON arrays
        return extractStringsFromJson(s);
    }

    /**
     * Extract content from JSON array string (handling non-standard JSON returned by LLM)
     */
    private List<String> extractStringsFromJson(String json) {
        List<String> result = new ArrayList<>();
        
        // Remove the outer [ ]
        String content = json;
        if (content.startsWith("[")) {
            content = content.substring(1);
        }
        if (content.endsWith("]")) {
            content = content.substring(0, content.length() - 1);
        }
        
        // Matches strings in the "..." format, handling escaped quotes
        //Use a more robust pattern: match ends with " Start to next unescaped "
        Pattern pattern = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            try {
                String text = matcher.group(1);
                //Handle common JSON escapes
                text = text
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
                
                if (StrUtil.isNotBlank(text) && text.length() > 20) {
                    result.add(text.trim());
                }
            } catch (Exception e) {
                // Ignore single parsing errors
            }
        }
        
        if (!result.isEmpty()) {
            log.info("Regularly extracted {} fragments", result.size());
        }
        
        return result;
    }
}

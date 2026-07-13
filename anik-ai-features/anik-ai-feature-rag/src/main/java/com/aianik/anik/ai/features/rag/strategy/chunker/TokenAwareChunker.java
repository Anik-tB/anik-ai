package com.aianik.anik.ai.features.rag.strategy.chunker;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.common.util.JsonUtil;
import com.aianik.anik.ai.features.rag.dto.ChunkDTO;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The secondary recursive segmentation and tool methods shared by all {@link ChunkStrategy} are no longer used as external slicing entrances.
 */
@Slf4j
@Component
public class TokenAwareChunker {

    /**
     * Recursively split the paragraph array by maxTokens.
     */
    public List<ChunkDTO> chunkParagraphs(String[] paragraphs, int maxTokens, int overlap) {
        DocumentSplitter splitter = DocumentSplitters.recursive(maxTokens, overlap);
        List<ChunkDTO> chunks = new ArrayList<>();

        for (int pIdx = 0; pIdx < paragraphs.length; pIdx++) {
            String paragraph = paragraphs[pIdx].trim();
            if (paragraph.isEmpty()) {
                continue;
            }

            List<TextSegment> segments = splitter.split(Document.from(paragraph));
            for (int cIdx = 0; cIdx < segments.size(); cIdx++) {
                String text = segments.get(cIdx).text().trim();
                if (text.isEmpty()) {
                    continue;
                }
                chunks.add(ChunkDTO.builder()
                        .paragraphIndex(pIdx)
                        .chunkIndex(cIdx)
                        .content(text)
                        .tokenCount(estimateTokens(text))
                        .build());
            }
        }
        return chunks;
    }

    /**
     * When delimiter is null, it is divided by paragraph \\n\\n; when it is an ordinary string, it is divided by this string; if it starts with [, it is parsed into a JSON string array and divided by any match (long strings are given priority).
     */
    public List<String> resolveDelimiterList(String raw) {
        if (StrUtil.isBlank(raw)) {
            return List.of("\n\n");
        }
        String s = raw.trim();
        if (s.startsWith("[")) {
            try {
                List<String> list = JsonUtil.parseList(s, String.class);
                List<String> filtered = list.stream().filter(StrUtil::isNotBlank).map(String::trim).toList();
                if (filtered.isEmpty()) {
                    return List.of("\n\n");
                }
                ArrayList<String> sorted = new ArrayList<>(filtered);
                sorted.sort(Comparator.comparingInt(String::length).reversed());
                return sorted;
            } catch (Exception e) {
                log.warn("Failed to parse multi-delimiter JSON, split by literal: {}", e.getMessage());
                return List.of(s);
            }
        }
        return List.of(s);
    }

    /**
     * Split content by multiple delimiters (regular or literal).
     */
    public String[] splitByAnyDelimiter(String content, List<String> delimiters) {
        if (delimiters.isEmpty()) {
            return new String[] { content };
        }
        if (delimiters.size() == 1) {
            return content.split(Pattern.quote(delimiters.get(0)));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < delimiters.size(); i++) {
            if (i > 0) {
                sb.append('|');
            }
            sb.append(Pattern.quote(delimiters.get(i)));
        }
        return Pattern.compile(sb.toString()).split(content);
    }

    /** CJK-aware token estimation*/
    public int estimateTokens(String text) {
        int cjkCount = 0;
        int asciiWordCount = 0;
        boolean inWord = false;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                cjkCount++;
                inWord = false;
            } else if (Character.isLetterOrDigit(c)) {
                if (!inWord) {
                    asciiWordCount++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        return (int) (cjkCount * 1.5) + asciiWordCount;
    }
}

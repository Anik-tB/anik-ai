package com.aizuda.anik.ai.features.rag.strategy.parser;

import com.aizuda.anik.ai.common.execption.AnikAiException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class MarkdownParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "md".equalsIgnoreCase(fileType) || "markdown".equalsIgnoreCase(fileType);
    }

    @Override
    public String parse(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String raw = reader.lines().collect(Collectors.joining("\n"));
            return cleanMarkdown(raw);
        } catch (Exception e) {
            throw new AnikAiException("Failed to parse markdown", e);
        }
    }

    private String cleanMarkdown(String md) {
        String clean = md;
        // Remove the title tag and keep the title text
        clean = clean.replaceAll("(?m)^#+\\s*", "");
        // Remove the code block fence mark (```language) and retain the code content
        clean = clean.replaceAll("(?m)^```[a-zA-Z]*\\s*$", "");
        // Remove inline code backticks and retain code text
        clean = clean.replaceAll("`([^`]+)`", "$1");
        //Remove bold mark
        clean = clean.replaceAll("\\*\\*(.*?)\\*\\*", "$1");
        // Remove italics
        clean = clean.replaceAll("\\*(.*?)\\*", "$1");
        // Remove the picture syntax, and the picture has no textual semantics
        clean = clean.replaceAll("!\\[.*?]\\(.*?\\)", "");
        // Remove link syntax, keep link text
        clean = clean.replaceAll("\\[(.*?)]\\(.*?\\)", "$1");
        // Remove reference mark symbols and retain reference text
        clean = clean.replaceAll("(?m)^>\\s*", "");
        // Remove list mark symbols and retain list text
        clean = clean.replaceAll("(?m)^[-*+]\\s+", "");
        return clean;
    }
}

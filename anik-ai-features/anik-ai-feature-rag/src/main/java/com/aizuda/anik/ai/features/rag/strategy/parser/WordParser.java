package com.aizuda.anik.ai.features.rag.strategy.parser;

import com.aizuda.anik.ai.common.execption.AnikAiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class WordParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "docx".equalsIgnoreCase(fileType);
    }

    @Override
    public String parse(InputStream inputStream) {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (Exception e) {
            throw new AnikAiException("Failed to parse Word document", e);
        }
    }
}

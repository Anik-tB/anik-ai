package com.aianik.anik.ai.features.rag.strategy.importer;

public interface DocumentImportStrategy {

    boolean supports(String sourceType);

    ImportResult importDocument(ImportRequest request);
}

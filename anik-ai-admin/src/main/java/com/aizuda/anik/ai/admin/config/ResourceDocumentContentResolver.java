package com.aizuda.anik.ai.admin.config;

import com.aizuda.anik.ai.features.rag.pipeline.DocumentContentResolver;
import com.aizuda.anik.ai.features.resource.ResourceService;
import com.aizuda.anik.ai.persistence.rag.po.RagDocumentPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Load document content through the resource library.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class ResourceDocumentContentResolver implements DocumentContentResolver {

    private final ResourceService resourceService;

    @Override
    public InputStream resolve(RagDocumentPO document) {
        if (document.getResourceId() == null) {
            return null;
        }
        return resourceService.load(document.getResourceId());
    }
}

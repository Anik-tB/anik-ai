package com.aizuda.anik.ai.features.rag.pipeline;

import com.aizuda.anik.ai.persistence.rag.po.RagDocumentPO;

import java.io.InputStream;

/**
 * Document Content Parser SPI
 * Responsible for loading the file content stream according to how the document is stored.
 * The default implementation is RAG local storage (historical data downgrade), which can be overridden by the admin layer to use the resource library.
 */
public interface DocumentContentResolver {

    /**
     * Load the file corresponding to the document InputStream.
     * For TEXT type documents (content stored in the content field), null is returned.
     */
    InputStream resolve(RagDocumentPO document);
}

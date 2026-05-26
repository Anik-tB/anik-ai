package com.aizuda.anik.ai.features.rag.search.pipeline.handler;

import com.aizuda.anik.ai.features.rag.search.pipeline.RagSearchContext;
import com.aizuda.anik.ai.features.rag.search.pipeline.RagSearchHandler;
import com.aizuda.anik.ai.persistence.rag.dataobject.RagConfigDO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class QueryRewriteHandler implements RagSearchHandler {

    @Override
    public void handle(RagSearchContext ctx) {
        // Query rewriting functionality has been removed
    }
}

package com.aizuda.anik.ai.features.rag.strategy.chunker;

import com.aizuda.anik.ai.features.rag.enums.ChunkModeEnum;
import org.springframework.stereotype.Component;

/**
 * By length: No first-level segmentation is performed, and the entire article enters the second-level recursion as one paragraph.
 */
@Component
public class LengthChunkStrategy extends AbstractChunkStrategy {

    public LengthChunkStrategy(TokenAwareChunker chunker) {
        super(chunker);
    }

    @Override
    public boolean supports(ChunkModeEnum mode) {
        return ChunkModeEnum.DEFAULT == mode;
    }

    @Override
    protected String[] splitIntoParagraphs(ChunkContext ctx) {
        return new String[] { ctx.getContent() };
    }
}

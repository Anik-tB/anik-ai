package com.aizuda.anik.ai.features.rag.strategy.chunker;

import com.aizuda.anik.ai.features.rag.enums.ChunkModeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Split by delimiter level, then recursively split by length.
 */
@Component
public class DelimiterChunkStrategy extends AbstractChunkStrategy {

    public DelimiterChunkStrategy(TokenAwareChunker chunker) {
        super(chunker);
    }

    @Override
    public boolean supports(ChunkModeEnum mode) {
        return ChunkModeEnum.DELIMITER == mode;
    }

    @Override
    protected String[] splitIntoParagraphs(ChunkContext ctx) {
        List<String> delimiters = chunker.resolveDelimiterList(ctx.getCustomDelimiter());
        return chunker.splitByAnyDelimiter(ctx.getContent(), delimiters);
    }
}

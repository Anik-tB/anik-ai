package com.aizuda.anik.ai.features.rag.strategy.chunker;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.features.rag.dto.ChunkDTO;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Template method: The subclass implements first-level segmentation {@link #splitIntoParagraphs}, and the second-level recursive segmentation is handled uniformly by {@link TokenAwareChunker#chunkParagraphs}.
 */
@RequiredArgsConstructor
public abstract class AbstractChunkStrategy implements ChunkStrategy {

    protected final TokenAwareChunker chunker;

    @Override
    public List<ChunkDTO> chunk(ChunkContext context) {
        if (StrUtil.isBlank(context.getContent())) {
            return List.of();
        }
        String[] paragraphs = splitIntoParagraphs(context);
        return chunker.chunkParagraphs(paragraphs, context.getMaxTokens(), context.getOverlap());
    }

    /**
     * The subclass implements first-level segmentation logic and splits the document content into an array of paragraphs.
     * Second-level recursive segmentation (according to maxTokens) is uniformly called by abstract classes.
     */
    protected abstract String[] splitIntoParagraphs(ChunkContext context);
}

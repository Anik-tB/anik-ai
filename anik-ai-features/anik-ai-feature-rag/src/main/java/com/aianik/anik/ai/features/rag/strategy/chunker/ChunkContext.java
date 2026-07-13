package com.aianik.anik.ai.features.rag.strategy.chunker;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkContext {
    /** Full text of document to be sliced ​​*/
    private String content;
    /** Maximum number of tokens in a single fragment */
    private int maxTokens;
    /** Number of overlapping tokens between fragments */
    private int overlap;
    /** Custom delimiter when mode=delimiter (can be a JSON array)*/
    private String customDelimiter;
    /** Regular expression when mode=regex */
    private String chunkRegex;
    /** Dialogue model configuration ID when mode=smart*/
    private Long chunkModelId;
}

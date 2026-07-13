package com.aianik.anik.ai.features.rag.strategy.chunker;

import com.aianik.anik.ai.features.rag.dto.ChunkDTO;
import com.aianik.anik.ai.features.rag.enums.ChunkModeEnum;

import java.util.List;

public interface ChunkStrategy {

    /** Whether the slicing mode is supported */
    boolean supports(ChunkModeEnum mode);

    /** Execute slicing */
    List<ChunkDTO> chunk(ChunkContext context);
}

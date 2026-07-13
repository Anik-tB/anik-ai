package com.aianik.anik.ai.features.rag.strategy.chunker;

import com.aianik.anik.ai.features.rag.enums.ChunkModeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChunkStrategyFactory {

    private final List<ChunkStrategy> strategies;

    public ChunkStrategy getStrategy(ChunkModeEnum mode) {
        return strategies.stream()
                .filter(s -> s.supports(mode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported slicing modes: " + mode));
    }
}

package com.aizuda.anik.ai.features.rag.strategy.chunker;

import cn.hutool.core.util.StrUtil;
import com.aizuda.anik.ai.features.rag.enums.ChunkModeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Split by Java regularity at one level, and then recursively split by length. When the regular expression is empty, it falls back to \\n\\n separation.
 */
@Slf4j
@Component
public class RegexChunkStrategy extends AbstractChunkStrategy {

    public RegexChunkStrategy(TokenAwareChunker chunker) {
        super(chunker);
    }

    @Override
    public boolean supports(ChunkModeEnum mode) {
        return ChunkModeEnum.REGEX == mode;
    }

    @Override
    protected String[] splitIntoParagraphs(ChunkContext ctx) {
        String regex = StrUtil.trimToNull(ctx.getChunkRegex());
        if (regex == null) {
            log.warn("regex mode but pattern is empty, fallback to \\n\\n");
            List<String> delimiters = chunker.resolveDelimiterList("\n\n");
            return chunker.splitByAnyDelimiter(ctx.getContent(), delimiters);
        }
        final Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regular expression: " + e.getMessage(), e);
        }
        return pattern.split(ctx.getContent(), -1);
    }
}

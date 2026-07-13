package com.aianik.anik.ai.features.rag.dedup;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.features.rag.enums.DedupAction;
import com.aianik.anik.ai.features.rag.enums.DedupStrategy;
import com.aianik.anik.ai.persistence.rag.mapper.RagDocumentMapper;
import com.aianik.anik.ai.persistence.rag.po.RagDocumentPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Knowledge base document deduplication determiner
 * <p>
 * Single responsibility: Query conflict documents based on policies, and give final decision making combined with conflict actions.
 *
 * @author openanik
 */
@Component
@RequiredArgsConstructor
public class DocumentDedupChecker {

    private final RagDocumentMapper ragDocumentMapper;

    /**
     * Check for duplicates by policy
     *
     * @param ragId       RAG ID
     * @param fileName file name
     * @param contentHash file content SHA-256
     * @param strategy deduplication strategy
     * @return hit result (including conflicting documents)
     */
    public DedupResult check(Long ragId, String fileName, String contentHash, DedupStrategy strategy) {
        if (ragId == null || strategy == null || strategy == DedupStrategy.NONE) {
            return DedupResult.none();
        }

        RagDocumentPO byContent = null;
        if (strategy.matchesByContent() && StrUtil.isNotBlank(contentHash)) {
            byContent = ragDocumentMapper.selectOne(
                    new LambdaQueryWrapper<RagDocumentPO>()
                            .eq(RagDocumentPO::getRagId, ragId)
                            .eq(RagDocumentPO::getContentHash, contentHash)
                            .last("LIMIT 1"));
        }

        RagDocumentPO byName = null;
        if (strategy.matchesByName() && StrUtil.isNotBlank(fileName)) {
            byName = ragDocumentMapper.selectOne(
                    new LambdaQueryWrapper<RagDocumentPO>()
                            .eq(RagDocumentPO::getRagId, ragId)
                            .eq(RagDocumentPO::getName, fileName)
                            .last("LIMIT 1"));
        }

        if (byName != null && byContent != null && Objects.equals(byName.getId(), byContent.getId())) {
            return DedupResult.of(DedupMatchType.BOTH, byName);
        }
        // The same conflict will be reported first (more stringent); if both exist, it will be processed as BOTH (already returned in the above branch)
        if (byContent != null) {
            return DedupResult.of(DedupMatchType.BY_CONTENT, byContent);
        }
        if (byName != null) {
            return DedupResult.of(DedupMatchType.BY_NAME, byName);
        }
        return DedupResult.none();
    }

    /**
     * Combining hit results and conflict actions into final decision making
     */
    public UploadDecision decide(DedupResult result, DedupAction action) {
        if (!result.isHit()) {
            return UploadDecision.newFile();
        }
        DedupAction effective = action == null ? DedupAction.REJECT : action;
        return switch (effective) {
            case REJECT -> UploadDecision.reject(result);
            case SKIP -> UploadDecision.skip(result);
            case OVERWRITE -> UploadDecision.overwrite(result);
        };
    }
}

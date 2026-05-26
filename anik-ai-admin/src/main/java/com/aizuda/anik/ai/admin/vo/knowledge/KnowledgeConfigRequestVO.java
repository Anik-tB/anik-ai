package com.aizuda.anik.ai.admin.vo.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeConfigRequestVO {

    private SearchParams searchParams;

    private ModelParams modelParams;

    /** Document slicing strategy (written when creating the knowledge base) */
    private ChunkParams chunkParams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkParams {
        /** length | delimiter | regex | smart */
        private String mode;
        /** Maximum token/character size of a single fragment (consistent with recursive splitter) */
        private Integer maxChunkTokens;
        private Integer chunkOverlap;
        /** When mode=delimiter, it is used as a first-level delimiter */
        private String customDelimiter;
        /** First-level segmentation regularity (Java Pattern) when mode=regex */
        private String chunkRegex;
        /** Chat model config ID used for semantic chunking when mode=smart */
        private Long chunkModelId;
        private Boolean mergeShortSegments;
        private Boolean imageOcr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchParams {
        private Integer resultCount;
        private Boolean rerankEnabled;
        private Long rerankModelId;
        private Integer enterRerankCount;
        private Boolean thresholdEnabled;
        private Double threshold;
        private Double denseWeight;
        private String fusionStrategy;
        private Integer rrfK;
        /** Whether enabled question rewritten (original QaParams unique field)*/
        private Boolean questionRewrite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelParams {
        private Long modelId;
        private Integer nearbySliceCount;
        private String prompt;
    }
}

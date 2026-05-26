package com.aizuda.anik.ai.admin.vo.memory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemoryConfigRequestVO {

    @NotBlank
    private String name;

    private Integer status;

    private Long vectorStoreInstanceId;

    @NotNull
    private Long embeddingModelId;

    private Boolean searchEngineEnable;

    private Long searchEngineInstanceId;

    private Integer maxRecall;

    private Boolean rewriteEnabled;

    private Boolean rerankEnabled;

    private Long rerankModelId;

    private BigDecimal similarityThreshold;

    private Boolean thresholdEnabled;

    private Integer enterRerankCount;

    private String fusionStrategy;

    private Double denseWeight;

    private Integer rrfK;

    private Integer extractionInterval;

    private Integer maxMemoriesPerExtraction;

    private Long extractionModelId;

    private Integer memoryExpirationDays;

    /** Custom memory extraction prompt (uses default if empty) */
    private String customExtractionPrompt;

    private String customUpdatePrompt;

    /** @deprecated Deprecated, use customExtractionPrompt instead */
    @Deprecated
    private String extractionRuleType;

    /** @deprecated Deprecated, use customExtractionPrompt instead */
    @Deprecated
    private String extractionRuleInstruction;

    /** @deprecated Deprecated, use customExtractionPrompt instead */
    @Deprecated
    private String extractionPromptType;
}

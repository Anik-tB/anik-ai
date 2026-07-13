package com.aianik.anik.ai.admin.vo.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryConfigResponseVO {

    private Long id;
    private String name;
    private Integer status;
    private Long vectorStoreInstanceId;
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
    /** Whether a custom prompt is used (customExtractionPrompt is non-null) */
    private Boolean useCustomPrompt;
    private String customExtractionPrompt;
    private String customUpdatePrompt;
    private LocalDateTime createDt;
    private LocalDateTime updateDt;
}

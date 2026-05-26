package com.aizuda.anik.ai.admin.service.model;

import com.aizuda.anik.ai.admin.vo.model.AiModelUsageStatsQueryVO;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.persistence.model.mapper.AiModelConfigMapper;
import com.aizuda.anik.ai.persistence.model.mapper.AiModelUsageStatMapper;
import com.aizuda.anik.ai.admin.vo.model.AiModelUsageStatVO;
import com.aizuda.anik.ai.persistence.model.po.AiModelConfigPO;
import com.aizuda.anik.ai.persistence.model.po.AiModelUsageStatPO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI model usage statistics service
 * 
 * @author openanik
 * @since 2026-04-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelUsageService implements com.aizuda.anik.ai.model.service.AiModelUsageService {

    private final AiModelUsageStatMapper usageStatMapper;
    private final AiModelConfigMapper configMapper;



    /**
     * Get usage statistics for a specific model and user
     *
     * @param modelId model ID
     * @param userId  userID
     * @return usage statistics VO
     */
    public AiModelUsageStatVO getUsageStats(Long modelId, Long userId) {
        LambdaQueryWrapper<AiModelUsageStatPO> queryWrapper = new LambdaQueryWrapper<AiModelUsageStatPO>()
                .eq(AiModelUsageStatPO::getModelId, modelId)
                .eq(AiModelUsageStatPO::getUserId, userId);

        AiModelUsageStatPO stat = usageStatMapper.selectOne(queryWrapper);
        if (stat == null) {
            return null;
        }

        return enrichStatVO(convertToVO(stat));
    }

    /**
     * Get the user's model usage statistics list (pagination)
     * Only include the user's own statistics
     *
     * @param userId  userID
     * @param queryVO query conditions
     * @return paginated results
     */
    public PageResult<List<AiModelUsageStatVO>> getUserModelStats(Long userId, AiModelUsageStatsQueryVO queryVO) {
        PageDTO<AiModelUsageStatPO> page = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<AiModelUsageStatPO> queryWrapper = new LambdaQueryWrapper<AiModelUsageStatPO>()
                .eq(AiModelUsageStatPO::getUserId, userId)
                .orderByDesc(AiModelUsageStatPO::getUpdatedDt);

        PageDTO<AiModelUsageStatPO> result = usageStatMapper.selectPage(page, queryWrapper);
        List<AiModelUsageStatVO> records = result.getRecords().stream()
                .map(po -> enrichStatVO(convertToVO(po)))
                .collect(Collectors.toList());

        return new PageResult<>(result, records);
    }

    /**
     * Get the overall situation model usage statistics list (paging)
     * Only Admin can access
     *
     * @param queryVO query conditions
     * @return paginated results
     */
    public PageResult<List<AiModelUsageStatVO>> getGlobalModelStats(AiModelUsageStatsQueryVO queryVO) {
        PageDTO<AiModelUsageStatPO> page = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<AiModelUsageStatPO> queryWrapper = new LambdaQueryWrapper<AiModelUsageStatPO>()
                .orderByDesc(AiModelUsageStatPO::getTotalCalls)
                .orderByDesc(AiModelUsageStatPO::getUpdatedDt);

        PageDTO<AiModelUsageStatPO> result = usageStatMapper.selectPage(page, queryWrapper);
        List<AiModelUsageStatVO> records = result.getRecords().stream()
                .map(po -> enrichStatVO(convertToVO(po)))
                .collect(Collectors.toList());

        return new PageResult<>(result, records);
    }

    /**
     * Get usage statistics by model type
     *
     * @param modelType model type
     * @param startDate start date
     * @param endDate end date
     * @return statistics list
     */
    public List<AiModelUsageStatVO> getStatsByModelType(String modelType, LocalDate startDate, LocalDate endDate) {
        //First get all models of this type
        List<AiModelConfigPO> models = configMapper.selectList(
                new LambdaQueryWrapper<AiModelConfigPO>()
                        .eq(AiModelConfigPO::getModelType, modelType)
        );

        if (models.isEmpty()) {
            return List.of();
        }

        // Get statistics for each model
        return models.stream()
                .flatMap(model -> {
                    List<AiModelUsageStatPO> stats = usageStatMapper.selectList(
                            new LambdaQueryWrapper<AiModelUsageStatPO>()
                                    .eq(AiModelUsageStatPO::getModelId, model.getId())
                                    .between(AiModelUsageStatPO::getCreatedDt,
                                            startDate.atStartOfDay(),
                                            endDate.plusDays(1).atStartOfDay())
                    );
                    return stats.stream().map(po -> enrichStatVO(convertToVO(po)));
                })
                .collect(Collectors.toList());
    }

    /**
     * Rich statistical VO information
     * Add model name, type, provider and other information
     */
    private AiModelUsageStatVO enrichStatVO(AiModelUsageStatVO vo) {
        if (vo == null) {
            return null;
        }

        //Get Model configuration information
        AiModelConfigPO config = configMapper.selectById(vo.getModelId());
        if (config != null) {
            vo.setModelName(config.getModelName());
            vo.setModelType(config.getModelType());
            vo.setProviderId(config.getProviderId());

            // Calculate success rate (0-100)
            if (vo.getTotalCalls() != null && vo.getTotalCalls() > 0) {
                Double successRate = (vo.getSuccessCalls() * 100.0) / vo.getTotalCalls();
                vo.setSuccessRate(Math.round(successRate * 100.0) / 100.0);
            } else {
                vo.setSuccessRate(0.0);
            }
        }

        return vo;
    }

    /**
     * Convert PO to VO
     */
    private AiModelUsageStatVO convertToVO(AiModelUsageStatPO po) {
        if (po == null) {
            return null;
        }

        return AiModelUsageStatVO.builder()
                .id(po.getId())
                .modelId(po.getModelId())
                .userId(po.getUserId())
                .totalCalls(po.getTotalCalls())
                .successCalls(po.getSuccessCalls())
                .failedCalls(po.getFailedCalls())
                .totalTokensUsed(po.getTotalTokensUsed())
                .totalCost(po.getTotalCost())
                .avgResponseTime(po.getAvgResponseTime())
                .lastUsedDt(po.getLastUsedDt() != null ?
                        po.getLastUsedDt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .createdDt(po.getCreatedDt() != null ?
                        po.getCreatedDt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .updatedDt(po.getUpdatedDt() != null ?
                        po.getUpdatedDt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .build();
    }

    // ========== Inner classes ==========

    /**
     * Model-user key combination
     */
    private static class ModelUserKey {
        final String modelKey;
        final Long userId;

        ModelUserKey(String modelKey, Long userId) {
            this.modelKey = modelKey;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModelUserKey that = (ModelUserKey) o;
            return Objects.equals(modelKey, that.modelKey) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(modelKey, userId);
        }
    }

    /**
     * Usage aggregation
     */
    private static class UsageAggregation {
        long totalCalls = 0;
        long successCalls = 0;
        long failedCalls = 0;
        long totalInputTokens = 0;
        long totalOutputTokens = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        long totalResponseTime = 0;
        LocalDateTime lastUsedTime = null;
    }

    // ========== Deprecated method (interface signature retained for compatibility) ==========

    /**
     * @deprecated Deprecated. The data is automatically collected by the observational system without manual call.
     */
    @Deprecated
    @Override
    public void recordUsage(Long modelId, Long userId, Integer promptTokens, Integer completionTokens,
                            Long responseTime, Integer status, String errorMessage, String conversationId) {
        log.warn("recordUsage() is deprecated. Data is now collected by observability system automatically.");
    }
}

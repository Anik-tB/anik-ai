package com.aizuda.anik.ai.admin.service.model;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.common.util.JsonUtil;
import com.aizuda.anik.ai.common.util.StreamUtils;
import com.aizuda.anik.ai.persistence.model.mapper.AiModelConfigMapper;
import com.aizuda.anik.ai.persistence.model.mapper.AiModelProviderMapper;
import com.aizuda.anik.ai.common.model.ConfigExtAttrsDTO;
import com.aizuda.anik.ai.model.enums.ModelScopeEnum;
import com.aizuda.anik.ai.persistence.model.po.AiModelConfigPO;
import com.aizuda.anik.ai.persistence.model.po.AiModelProviderPO;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigRequestVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigQueryVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigVO;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI model configuration management service
 *
 * Function:
 * - Model CRUD operations
 * - API Key encrypted storage
 * - Query and switch default models by type
 * - Redis cache management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelConfigService {

    private final AiModelConfigMapper configMapper;
    private final AiModelProviderMapper providerMapper;
    private final com.aizuda.anik.ai.model.model.CryptoHelper cryptoHelper;

    /**
     * NewModel configuration (Admin permissions)
     */
    @Transactional
    public Long addModelConfig(AiModelConfigRequestVO requestVO) {
        log.info("Add model configuration: modelName={}, modelType={}, provider={}",
                requestVO.getModelName(), requestVO.getModelType(), requestVO.getProviderId());

        // Verify that the provider exists
        if (!validateProviderExists(requestVO.getProviderId())) {
            log.error("Provider does not exist: {}", requestVO.getProviderId());
            throw new AnikAiException("Provider does not exist");
        }

        Assert.notBlank(requestVO.getApiKey(), () -> new AnikAiException("apiKey is blank"));
        //Encrypt API Key
        String encryptedKey = encryptApiKey(requestVO.getApiKey());

        AiModelConfigPO config = AiModelConfigPO.builder()
                .providerId(requestVO.getProviderId())
                .modelName(requestVO.getModelName())
                .modelKey(requestVO.getModelKey())
                .modelType(requestVO.getModelType())
                .description(requestVO.getDescription())
                .apiKey(encryptedKey)
                .apiEndpoint(requestVO.getApiEndpoint())
                .configJson(JsonUtil.toJsonString(requestVO.getConfigJson()))
                .ownerId(requestVO.getOwnerId())
                .scope(requestVO.getScope() != null ?
                        requestVO.getScope() :
                        ModelScopeEnum.GLOBAL.getValue())
                .isDefault(requestVO.getIsDefault() != null ?
                        requestVO.getIsDefault() :
                        false)
                .isEnabled(true)
                .createdDt(LocalDateTime.now())
                .updatedDt(LocalDateTime.now())
                .build();

        configMapper.insert(config);
        log.info("Added model configuration successfully: id={}", config.getId());

        // Clear the cache of the corresponding type
        if (config.getIsDefault()) {
            clearCacheByType(config.getModelType());
        }

        return config.getId();
    }

    /**
     * renewModel configuration
     */
    @Transactional
    public boolean updateModelConfig(Long modelId, AiModelConfigRequestVO requestVO) {
        log.info("Update model configuration: id={}", modelId);

        AiModelConfigPO existing = configMapper.selectById(modelId);
        if (existing == null) {
            log.error("Model does not exist: {}", modelId);
            throw new AnikAiException("Model configuration does not exist");
        }

        // Encrypted API Key (if a new one is provided)
        String apiKey = StringUtils.hasText(requestVO.getApiKey()) ?
                encryptApiKey(requestVO.getApiKey()) :
                existing.getApiKey();

        AiModelConfigPO config = AiModelConfigPO.builder()
                .id(modelId)
                .providerId(requestVO.getProviderId() != null ? requestVO.getProviderId() : existing.getProviderId())
                .modelName(requestVO.getModelName() != null ? requestVO.getModelName() : existing.getModelName())
                .modelKey(requestVO.getModelKey() != null ? requestVO.getModelKey() : existing.getModelKey())
                .modelType(requestVO.getModelType() != null ? requestVO.getModelType() : existing.getModelType())
                .description(requestVO.getDescription() != null ? requestVO.getDescription() : existing.getDescription())
                .apiKey(apiKey)
                .apiEndpoint(requestVO.getApiEndpoint() != null ? requestVO.getApiEndpoint() : existing.getApiEndpoint())
                .configJson(JsonUtil.toJsonString(requestVO.getConfigJson()))
                .isDefault(requestVO.getIsDefault() != null ? requestVO.getIsDefault() : existing.getIsDefault())
                .updatedDt(LocalDateTime.now())
                .build();

        boolean success = configMapper.updateById(config) > 0;
        if (success) {
            clearCacheByType(config.getModelType());
        }
        return success;
    }

    /**
     * deleteModel configuration
     */
    @Transactional
    public boolean deleteModelConfig(Long modelId) {
        log.info("Delete model configuration: id={}", modelId);

        AiModelConfigPO config = configMapper.selectById(modelId);
        if (config == null) {
            return false;
        }

        boolean success = configMapper.deleteById(modelId) > 0;
        if (success && config.getIsDefault()) {
            clearCacheByType(config.getModelType());
        }
        return success;
    }

    /**
     * Get a single model configuration
     */
    public AiModelConfigVO getModelConfig(Long modelId) {
        AiModelConfigPO po = configMapper.selectById(modelId);
        if (po == null) {
            return null;
        }
        return convertToVO(po);
    }

    /**
     * Pagination query model configuration
     * Support filtering by providerKey, modelType, scope
     */
    public PageResult<List<AiModelConfigVO>> listModelConfigs(AiModelConfigQueryVO queryVO) {
        PageDTO<AiModelConfigPO> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        log.debug("Query model configuration: page={}, size={}, providerKey={}, modelType={}, scope={}",
                queryVO.getPage(), queryVO.getSize(), queryVO.getProviderKey(), queryVO.getModelType(), queryVO.getScope());

        LambdaQueryWrapper<AiModelConfigPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryVO.getProviderKey())) {
            AiModelProviderPO provider = providerMapper.selectOne(
                    new LambdaQueryWrapper<AiModelProviderPO>()
                            .eq(AiModelProviderPO::getProviderKey, queryVO.getProviderKey()));
            if (provider != null) {
                wrapper.eq(AiModelConfigPO::getProviderId, provider.getId());
            }
        }

        wrapper.eq(StringUtils.hasText(queryVO.getModelType()), AiModelConfigPO::getModelType, queryVO.getModelType())
                .eq(StringUtils.hasText(queryVO.getScope()), AiModelConfigPO::getScope, queryVO.getScope())
                .between(ObjUtil.isNotNull(queryVO.getStartDt()) && ObjUtil.isNotNull(queryVO.getEndDt()),
                        AiModelConfigPO::getCreatedDt, queryVO.getStartDt(), queryVO.getEndDt())
                .orderByDesc(AiModelConfigPO::getIsDefault)
                .orderByAsc(AiModelConfigPO::getCreatedDt);

        Page<AiModelConfigPO> page = configMapper.selectPage(pageDTO, wrapper);

        List<Long> providerIds = page.getRecords().stream().map(AiModelConfigPO::getProviderId).toList();
        if (CollectionUtils.isEmpty(providerIds)) {
            return new PageResult<>();
        }
        List<AiModelProviderPO> aiModelProviderPOS = providerMapper.selectByIds(providerIds);
        Map<Long, AiModelProviderPO> providerPOMap = StreamUtils.toIdentityMap(aiModelProviderPOS, AiModelProviderPO::getId);

        List<AiModelConfigVO> records = page.convert(this::convertToVO).getRecords();
        for (AiModelConfigVO record : records) {
            AiModelProviderPO aiModelProviderPO = providerPOMap.get(record.getProviderId());
            if (aiModelProviderPO != null) {
                record.setProviderName(aiModelProviderPO.getProviderName());
            }
        }

        return new PageResult<>(pageDTO, records);
    }

    /**
     * Query enabled models by model type
     */
    public List<AiModelConfigVO> getModelsByType(String modelType) {
        log.debug("Query the model by type: modelType={}", modelType);

        List<Long> enabledProviderIds = getEnabledProviderIds();
        if (enabledProviderIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<AiModelConfigPO> wrapper = new LambdaQueryWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getModelType, modelType)
                .eq(AiModelConfigPO::getIsEnabled, true)
                .in(AiModelConfigPO::getProviderId, enabledProviderIds)
                .orderByDesc(AiModelConfigPO::getCreatedDt);

        List<AiModelConfigPO> poList = configMapper.selectList(wrapper);
        return poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * Query by provider and model type
     */
    public List<AiModelConfigVO> getModelsByProviderAndType(String provider, String modelType) {
        log.debug("Query models by provider and type: provider={}, modelType={}", provider, modelType);

        AiModelProviderPO providerPO = providerMapper.selectOne(
                new LambdaQueryWrapper<AiModelProviderPO>()
                        .eq(AiModelProviderPO::getProviderKey, provider)
                        .eq(AiModelProviderPO::getIsEnabled, true));
        if (providerPO == null) {
            return List.of();
        }

        LambdaQueryWrapper<AiModelConfigPO> wrapper = new LambdaQueryWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getProviderId, providerPO.getId())
                .eq(AiModelConfigPO::getModelType, modelType)
                .eq(AiModelConfigPO::getIsEnabled, true)
                .orderByDesc(AiModelConfigPO::getCreatedDt);

        List<AiModelConfigPO> poList = configMapper.selectList(wrapper);
        return poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * Get the global default model
     */
    public AiModelConfigVO getDefaultModel() {
        log.debug("Get the global default model");

        List<Long> enabledProviderIds = getEnabledProviderIds();
        if (enabledProviderIds.isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<AiModelConfigPO> wrapper = new LambdaQueryWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getIsDefault, true)
                .eq(AiModelConfigPO::getScope, ModelScopeEnum.GLOBAL.getValue())
                .eq(AiModelConfigPO::getIsEnabled, true)
                .in(AiModelConfigPO::getProviderId, enabledProviderIds)
                .last("LIMIT 1");

        AiModelConfigPO po = configMapper.selectOne(wrapper);
        return po != null ? convertToVO(po) : null;
    }

    /**
     * Get default model by type
     */
    public AiModelConfigVO getDefaultModelByType(String modelType) {
        log.debug("Get the default model by type: modelType={}", modelType);

        List<Long> enabledProviderIds = getEnabledProviderIds();
        if (enabledProviderIds.isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<AiModelConfigPO> wrapper = new LambdaQueryWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getModelType, modelType)
                .eq(AiModelConfigPO::getIsDefault, true)
                .eq(AiModelConfigPO::getIsEnabled, true)
                .in(AiModelConfigPO::getProviderId, enabledProviderIds)
                .last("LIMIT 1");

        AiModelConfigPO po = configMapper.selectOne(wrapper);
        return po != null ? convertToVO(po) : null;
    }

    /**
     * Switch default model
     */
    @Transactional
    public boolean switchDefaultModel(Long modelId) {
        log.info("Switch default model: id={}", modelId);

        AiModelConfigPO config = configMapper.selectById(modelId);
        if (config == null || !config.getIsEnabled()) {
            log.error("Model does not exist or is disabled: {}", modelId);
            throw new AnikAiException("Model does not exist or is disabled");
        }

        // Clear other default tags for this type
        LambdaUpdateWrapper<AiModelConfigPO> updateWrapper = new LambdaUpdateWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getModelType, config.getModelType())
                .set(AiModelConfigPO::getIsDefault, false);
        configMapper.update(null, updateWrapper);

        // Set this model as default
        config.setIsDefault(true);
        config.setUpdatedDt(LocalDateTime.now());
        boolean success = configMapper.updateById(config) > 0;

        if (success) {
            clearCacheByType(config.getModelType());
        }

        return success;
    }

    /**
     * Get user's personalModel configuration
     */
    public List<AiModelConfigVO> getPersonalModels(Long userId) {
        log.debug("Get the user's personal model configuration: userId={}", userId);

        LambdaQueryWrapper<AiModelConfigPO> wrapper = new LambdaQueryWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getOwnerId, userId)
                .eq(AiModelConfigPO::getScope, ModelScopeEnum.PERSONAL.getValue())
                .eq(AiModelConfigPO::getIsEnabled, true)
                .orderByDesc(AiModelConfigPO::getCreatedDt);

        List<AiModelConfigPO> poList = configMapper.selectList(wrapper);
        return poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ==================== Private methods ====================

    /**
     * Encrypt API Key
     */
    private String encryptApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return "";
        }
        return cryptoHelper.encrypt(apiKey);
    }

    /**
     * Verify that the provider exists
     */
    private boolean validateProviderExists(Long providerId) {
        return providerMapper.selectById(providerId) != null;
    }

    private List<Long> getEnabledProviderIds() {
        return providerMapper.selectList(
                new LambdaQueryWrapper<AiModelProviderPO>()
                        .eq(AiModelProviderPO::getIsEnabled, true)
                        .select(AiModelProviderPO::getId))
                .stream()
                .map(AiModelProviderPO::getId)
                .toList();
    }

    /**
     * Clear cache of specific types
     */
    private void clearCacheByType(String modelType) {
        // Redis cache clearing logic can be integrated here
        log.debug("Clear cache: modelType={}", modelType);
    }

    /**
     * Convert to VO (hide sensitive information)
     */
    private AiModelConfigVO convertToVO(AiModelConfigPO po) {

        return AiModelConfigVO.builder()
                .id(po.getId())
                .providerId(po.getProviderId())
                .modelName(po.getModelName())
                .modelKey(po.getModelKey())
                .modelType(po.getModelType())
                .description(po.getDescription())
                .apiEndpoint(po.getApiEndpoint())
                .configJson(JsonUtil.parseObject(Optional.ofNullable(po.getConfigJson()).orElse("{}"), ConfigExtAttrsDTO.class))
                .ownerId(po.getOwnerId())
                .scope(po.getScope())
                .isDefault(po.getIsDefault())
                .isEnabled(po.getIsEnabled())
                .createdDt(po.getCreatedDt())
                .updatedDt(po.getUpdatedDt())
                .build();
    }

    /**
     * Desensitization API Key
     * Only the first 4 digits and the last 4 digits are displayed
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        String prefix = apiKey.substring(0, 4);
        String suffix = apiKey.substring(apiKey.length() - 4);
        int middleLength = apiKey.length() - 8;
        return prefix + "*".repeat(Math.max(0, middleLength)) + suffix;
    }

    /**
     * Only renew model extension configuration (such as automatically writing embedding dimensions)
     */
    @Transactional
    public void updateConfigJson(Long modelId, ConfigExtAttrsDTO configJson) {
        AiModelConfigPO po = configMapper.selectById(modelId);
        if (po == null) {
            throw new AnikAiException("Model configuration does not exist");
        }
        po.setConfigJson(JsonUtil.toJsonString(configJson));
        po.setUpdatedDt(LocalDateTime.now());
        configMapper.updateById(po);
        clearCacheByType(po.getModelType());
    }

    /**
     * enableModel configuration (Admin)
     */
    @Transactional
    public boolean enableModelConfig(Long modelId) {
        log.info("Enable model configuration: id={}", modelId);

        AiModelConfigPO config = configMapper.selectById(modelId);
        if (config == null) {
            log.error("Model configuration does not exist: {}", modelId);
            throw new AnikAiException("Model configuration does not exist");
        }

        if (config.getIsEnabled()) {
            log.debug("Model configuration is already enabled: {}", modelId);
            return true;
        }

        LambdaUpdateWrapper<AiModelConfigPO> updateWrapper = new LambdaUpdateWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getId, modelId)
                .set(AiModelConfigPO::getIsEnabled, true)
                .set(AiModelConfigPO::getUpdatedDt, LocalDateTime.now());

        boolean success = configMapper.update(null, updateWrapper) > 0;
        if (success) {
            clearCacheByType(config.getModelType());
            log.info("Model configuration enabled successfully: id={}", modelId);
        }
        return success;
    }

    /**
     * DisableModel configuration (Admin)
     */
    @Transactional
    public boolean disableModelConfig(Long modelId) {
        log.info("Disable model configuration: id={}", modelId);

        AiModelConfigPO config = configMapper.selectById(modelId);
        if (config == null) {
            log.error("Model configuration does not exist: {}", modelId);
            throw new AnikAiException("Model configuration does not exist");
        }

        if (!config.getIsEnabled()) {
            log.debug("Model configuration is already disabled: {}", modelId);
            return true;
        }

        //If it is the default model, Disable is not allowed
        if (config.getIsDefault()) {
            log.error("Default model does not allow disabling: {}", modelId);
            throw new AnikAiException("The default model does not allow disabling, please switch to the default model first");
        }

        LambdaUpdateWrapper<AiModelConfigPO> updateWrapper = new LambdaUpdateWrapper<AiModelConfigPO>()
                .eq(AiModelConfigPO::getId, modelId)
                .set(AiModelConfigPO::getIsEnabled, false)
                .set(AiModelConfigPO::getUpdatedDt, LocalDateTime.now());

        boolean success = configMapper.update(null, updateWrapper) > 0;
        if (success) {
            clearCacheByType(config.getModelType());
            log.info("Model configuration disabled successfully: id={}", modelId);
        }
        return success;
    }
}

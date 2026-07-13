package com.aianik.anik.ai.admin.service.model;

import com.aianik.anik.ai.common.execption.AnikAiException;
import com.aianik.anik.ai.persistence.model.mapper.AiModelProviderMapper;
import com.aianik.anik.ai.persistence.model.po.AiModelProviderPO;
import com.aianik.anik.ai.admin.vo.model.AiModelProviderQueryVO;
import com.aianik.anik.ai.admin.vo.model.AiModelProviderVO;
import com.aianik.anik.ai.admin.vo.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI model provider management services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelProviderService {

    private final AiModelProviderMapper providerMapper;

    /**
     * New provider (Admin permissions)
     */
    @Transactional
    public Long addProvider(String providerName, String providerKey, String description, String iconUrl) {
        log.info("Add AI provider: name={}, key={}", providerName, providerKey);

        // Check if the provider already exists
        LambdaQueryWrapper<AiModelProviderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelProviderPO::getProviderKey, providerKey);
        AiModelProviderPO existing = providerMapper.selectOne(wrapper);
        if (existing != null) {
            log.warn("Provider already exists: {}", providerKey);
            throw new AnikAiException("Provider already exists: " + providerKey);
        }

        AiModelProviderPO provider = AiModelProviderPO.builder()
                .providerName(providerName)
                .providerKey(providerKey)
                .description(description)
                .iconUrl(iconUrl)
                .isEnabled(true)
                .createdDt(LocalDateTime.now())
                .updatedDt(LocalDateTime.now())
                .build();

        providerMapper.insert(provider);
        log.info("Provider added successfully: id={}", provider.getId());
        return provider.getId();
    }

    /**
     * New provider (receives VO object)
     */
    @Transactional
    public Long addProvider(AiModelProviderVO vo) {
        return addProvider(vo.getProviderName(), vo.getProviderKey(), vo.getDescription(), vo.getIconUrl());
    }

    /**
     * Get a list of all enabled providers (with cache)
     */
    public List<AiModelProviderVO> getProviderList() {
        log.debug("Query all enabled providers");

        LambdaQueryWrapper<AiModelProviderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelProviderPO::getIsEnabled, true)
                .orderByAsc(AiModelProviderPO::getCreatedDt);

        List<AiModelProviderPO> providers = providerMapper.selectList(wrapper);
        return providers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * Get all providers (including Disable, used for backend management)
     */
    public PageResult<List<AiModelProviderVO>> getAllProviders(AiModelProviderQueryVO queryVO) {
        PageDTO<AiModelProviderPO> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        LambdaQueryWrapper<AiModelProviderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AiModelProviderPO::getCreatedDt);
        PageDTO<AiModelProviderPO> page = providerMapper.selectPage(pageDTO, wrapper);
        return new PageResult<>(page,page.getRecords()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
    }

    /**
     * Get provider by ID
     */
    public AiModelProviderPO getProviderById(Long providerId) {
        if (providerId == null || providerId <= 0) {
            return null;
        }
        return providerMapper.selectById(providerId);
    }

    /**
     * Get provider VO by ID
     */
    public AiModelProviderVO getProvider(Long providerId) {
        AiModelProviderPO po = getProviderById(providerId);
        if (po == null) {
            log.warn("Provider does not exist: id={}", providerId);
            return null;
        }
        return convertToVO(po);
    }

    /**
     * Get provider by provider identifier
     */
    public AiModelProviderPO getProviderByKey(String providerKey) {
        if (providerKey == null || providerKey.isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<AiModelProviderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelProviderPO::getProviderKey, providerKey)
                .eq(AiModelProviderPO::getIsEnabled, true);
        return providerMapper.selectOne(wrapper);
    }

    /**
     * renewproviderenableStatus
     */
    @Transactional
    public boolean updateProviderStatus(Long providerId, Boolean enabled) {
        log.info("Update provider status: id={}, enabled={}", providerId, enabled);

        AiModelProviderPO provider = AiModelProviderPO.builder()
                .id(providerId)
                .isEnabled(enabled)
                .updatedDt(LocalDateTime.now())
                .build();

        return providerMapper.updateById(provider) > 0;
    }

    /**
     * renew provider (receives VO object)
     */
    @Transactional
    public boolean updateProvider(Long providerId, AiModelProviderVO vo) {
        log.info("Update AI provider: id={}, name={}", providerId, vo.getProviderName());

        AiModelProviderPO provider = AiModelProviderPO.builder()
                .id(providerId)
                .providerName(vo.getProviderName())
                .description(vo.getDescription())
                .iconUrl(vo.getIconUrl())
                .updatedDt(LocalDateTime.now())
                .build();

        return providerMapper.updateById(provider) > 0;
    }

    /**
     * deleteprovider
     */
    @Transactional
    public boolean deleteProvider(Long providerId) {
        log.info("Delete AI provider: id={}", providerId);

        if (providerId == null || providerId <= 0) {
            return false;
        }

        return providerMapper.deleteById(providerId) > 0;
    }

    /**
     * enableprovider
     */
    @Transactional
    public boolean enableProvider(Long providerId) {
        log.info("Enable provider: id={}", providerId);
        return updateProviderStatus(providerId, true);
    }

    /**
     * Disableprovider
     */
    @Transactional
    public boolean disableProvider(Long providerId) {
        log.info("Disable provider: id={}", providerId);
        return updateProviderStatus(providerId, false);
    }

    /**
     * Convert to VO
     */
    private AiModelProviderVO convertToVO(AiModelProviderPO po) {
        return AiModelProviderVO.builder()
                .id(po.getId())
                .providerName(po.getProviderName())
                .providerKey(po.getProviderKey())
                .description(po.getDescription())
                .iconUrl(po.getIconUrl())
                .isEnabled(po.getIsEnabled())
                .createdDt(po.getCreatedDt())
                .updatedDt(po.getUpdatedDt())
                .build();
    }
}

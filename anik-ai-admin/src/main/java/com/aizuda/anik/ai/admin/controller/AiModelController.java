package com.aizuda.anik.ai.admin.controller;

import com.aizuda.anik.ai.admin.security.annotation.LoginRequired;
import com.aizuda.anik.ai.admin.vo.PageResult;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigRequestVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigQueryVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelConfigVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelByProviderTypeQueryVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelProviderRequestVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelProviderVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelProviderQueryVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelUsageStatVO;
import com.aizuda.anik.ai.admin.vo.model.AiModelUsageStatsQueryVO;
import com.aizuda.anik.ai.admin.service.model.AiModelConfigService;
import com.aizuda.anik.ai.admin.service.model.AiModelProviderService;
import com.aizuda.anik.ai.admin.service.model.AiModelUsageService;
import com.aizuda.anik.ai.persistence.security.UserSessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI model management controller
 * Provides APIs such as model configuration CRUD, model switching, usage statistics, etc.
 */
@RestController
@RequestMapping("/ai-model")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelConfigService modelConfigService;
    private final AiModelProviderService modelProviderService;
    private final AiModelUsageService modelUsageService;

    /**
     * Get the list of providers (enabled only)
     * GET /ai-model/providers
     */
    @GetMapping("/providers")
    @LoginRequired
    public List<AiModelProviderVO> listProviders() {
        return modelProviderService.getProviderList();
    }

    /**
     * Get all providers (including Disable, used for backend management)
     * GET /ai-model/all-providers
     */
    @GetMapping("/all-providers")
    @LoginRequired
    public PageResult<List<AiModelProviderVO>> listAllProviders(AiModelProviderQueryVO queryVO) {
        return modelProviderService.getAllProviders(queryVO);
    }

    /**
     * Get a single provider
     * GET /ai-model/provider/{id}
     */
    @GetMapping("/provider/{id}")
    @LoginRequired
    public AiModelProviderVO getProvider(@PathVariable("id") Long id) {
        return modelProviderService.getProvider(id);
    }

    /**
     * NewProvider (Admin)
     * POST /ai-model/provider
     */
    @PostMapping("/provider")
    @LoginRequired
    public Long addProvider(@RequestBody @Validated AiModelProviderRequestVO requestVO) {
        return modelProviderService.addProvider(requestVO);
    }

    /**
     * renewprovider (Admin)
     * PUT /ai-model/provider/{id}
     */
    @PutMapping("/provider/{id}")
    @LoginRequired
    public boolean updateProvider(
            @PathVariable("id") Long id,
            @RequestBody @Validated AiModelProviderRequestVO requestVO) {
        return modelProviderService.updateProvider(id, requestVO);
    }

    /**
     * deleteProvider (Admin)
     * DELETE /ai-model/provider/{id}
     */
    @DeleteMapping("/provider/{id}")
    @LoginRequired
    public boolean deleteProvider(@PathVariable("id") Long id) {
        return modelProviderService.deleteProvider(id);
    }

    /**
     * enableprovider (Admin)
     * PUT /ai-model/provider/{id}/enable
     */
    @PutMapping("/provider/{id}/enable")
    @LoginRequired
    public boolean enableProvider(@PathVariable("id") Long id) {
        return modelProviderService.enableProvider(id);
    }

    /**
     * DisableProvider (Admin)
     * PUT /ai-model/provider/{id}/disable
     */
    @PutMapping("/provider/{id}/disable")
    @LoginRequired
    public boolean disableProvider(@PathVariable("id") Long id) {
        return modelProviderService.disableProvider(id);
    }

    /**
     * NewModel configuration (Admin)
     * POST /ai-model/config
     */
    @PostMapping("/config")
    @LoginRequired
    public Long addModelConfig(@RequestBody @Validated AiModelConfigRequestVO requestVO) {
        return modelConfigService.addModelConfig(requestVO);
    }

    /**
     * renewModel configuration (Admin)
     * PUT /ai-model/config/{id}
     */
    @PutMapping("/config/{id}")
    @LoginRequired
    public boolean updateModelConfig(
            @PathVariable("id") Long id,
            @RequestBody @Validated AiModelConfigRequestVO requestVO) {
        return modelConfigService.updateModelConfig(id, requestVO);
    }

    /**
     * deleteModel configuration (Admin)
     * DELETE /ai-model/config/{id}
     */
    @DeleteMapping("/config/{id}")
    @LoginRequired
    public boolean deleteModelConfig(@PathVariable("id") Long id) {
        return modelConfigService.deleteModelConfig(id);
    }

    /**
     * Get a single model configuration
     * GET /ai-model/config/{id}
     */
    @GetMapping("/config/{id}")
    @LoginRequired
    public AiModelConfigVO getModelConfig(@PathVariable("id") Long id) {
        return modelConfigService.getModelConfig(id);
    }

    /**
     * Paginated query model configuration list
     * GET /ai-model/configs?pageNum=1&pageSize=10&providerKey=openai&modelType=CHAT&scope=GLOBAL
     */
    @GetMapping("/configs")
    @LoginRequired
    public PageResult<List<AiModelConfigVO>> listModelConfigs(AiModelConfigQueryVO queryVO) {
        return modelConfigService.listModelConfigs(queryVO);
    }

    /**
     * Query model list by model type
     * GET /ai-model/by-type/CHAT
     */
    @GetMapping("/by-type/{modelType}")
    @LoginRequired
    public List<AiModelConfigVO> getModelsByType(@PathVariable("modelType") String modelType) {
        return modelConfigService.getModelsByType(modelType);
    }

    /**
     * Query models by provider and model type
     * GET /ai-model/by-provider-type?providerKey=openai&modelType=CHAT
     */
    @GetMapping("/by-provider-type")
    @LoginRequired
    public List<AiModelConfigVO> getModelsByProviderAndType(AiModelByProviderTypeQueryVO queryVO) {
        return modelConfigService.getModelsByProviderAndType(queryVO.getProviderKey(), queryVO.getModelType());
    }

    /**
     * Get the global default model
     * GET /ai-model/default
     */
    @GetMapping("/default")
    @LoginRequired
    public AiModelConfigVO getDefaultModel() {
        return modelConfigService.getDefaultModel();
    }

    /**
     * Get default model by type
     * GET /ai-model/default/CHAT
     */
    @GetMapping("/default/{modelType}")
    @LoginRequired
    public AiModelConfigVO getDefaultModelByType(@PathVariable("modelType") String modelType) {
        return modelConfigService.getDefaultModelByType(modelType);
    }

    /**
     * Switch default model (Admin)
     * PUT /ai-model/switch-default/{modelId}
     */
    @PutMapping("/switch-default/{modelId}")
    @LoginRequired
    public boolean switchDefaultModel(@PathVariable("modelId") Long modelId) {
        return modelConfigService.switchDefaultModel(modelId);
    }

    /**
     * enableModel configuration (Admin)
     * PUT /ai-model/config/{id}/enable
     */
    @PutMapping("/config/{id}/enable")
    @LoginRequired
    public boolean enableModelConfig(@PathVariable("id") Long id) {
        return modelConfigService.enableModelConfig(id);
    }

    /**
     * DisableModel configuration (Admin)
     * PUT /ai-model/config/{id}/disable
     */
    @PutMapping("/config/{id}/disable")
    @LoginRequired
    public boolean disableModelConfig(@PathVariable("id") Long id) {
        return modelConfigService.disableModelConfig(id);
    }

    /**
     * Get usage statistics for a specific model
     * GET /ai-model/usage/stat/{modelId}
     */
    @GetMapping("/usage/stat/{modelId}")
    @LoginRequired
    public AiModelUsageStatVO getModelUsageStat(@PathVariable("modelId") Long modelId) {
        Long userId = UserSessionUtils.currentUserSession().getId();
        return modelUsageService.getUsageStats(modelId, userId);
    }

    /**
     * Get the model usage statistics list of the current user (paging)
     * GET /ai-model/usage/user-stats?pageNum=1&pageSize=10
     */
    @GetMapping("/usage/user-stats")
    @LoginRequired
    public PageResult<List<AiModelUsageStatVO>> getUserModelStats(AiModelUsageStatsQueryVO queryVO) {
        Long userId = UserSessionUtils.currentUserSession().getId();
        return modelUsageService.getUserModelStats(userId, queryVO);
    }

    /**
     * Get the overall situation model usage statistics list (paging, Admin)
     * GET /ai-model/usage/global-stats?pageNum=1&pageSize=10
     */
    @GetMapping("/usage/global-stats")
    @LoginRequired
    public PageResult<List<AiModelUsageStatVO>> getGlobalModelStats(AiModelUsageStatsQueryVO queryVO) {
        return modelUsageService.getGlobalModelStats(queryVO);
    }
}

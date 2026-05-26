package com.aizuda.anik.ai.features.resource;

import com.aizuda.anik.ai.common.execption.AnikAiException;
import com.aizuda.anik.ai.features.resource.strategy.ResourceStorageFactory;
import com.aizuda.anik.ai.features.resource.strategy.ResourceStorageService;
import com.aizuda.anik.ai.features.resource.util.MimeTypeUtils;
import com.aizuda.anik.ai.persistence.resource.mapper.ResourceMapper;
import com.aizuda.anik.ai.persistence.resource.po.ResourcePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceMapper resourceMapper;
    private final ResourceStorageFactory storageFactory;

    @Transactional
    public ResourcePO upload(InputStream inputStream, String originalName, long fileSize,
                             String bizType, Long bizId, Long creatorId) {
        ResourceStorageService storage = storageFactory.getDefault();
        String storageKey = storage.store(bizType, originalName, inputStream);
        String mimeType = MimeTypeUtils.detect(originalName);

        ResourcePO resource = ResourcePO.builder()
                .storageKey(storageKey)
                .originalName(originalName)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .storageType(storage.getType())
                .bizType(bizType)
                .bizId(bizId)
                .creatorId(creatorId)
                .build();
        resourceMapper.insert(resource);

        String accessUrl = storage.getAccessUrl(resource.getId(), storageKey);
        resource.setAccessUrl(accessUrl);
        resourceMapper.updateById(resource);

        return resource;
    }

    public InputStream load(Long resourceId) {
        ResourcePO resource = requireResource(resourceId);
        ResourceStorageService storage = storageFactory.get(resource.getStorageType());
        return storage.load(resource.getStorageKey());
    }

    @Transactional
    public void delete(Long resourceId) {
        ResourcePO resource = resourceMapper.selectById(resourceId);
        if (resource == null) return;
        try {
            ResourceStorageService storage = storageFactory.get(resource.getStorageType());
            storage.delete(resource.getStorageKey());
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", resource.getStorageKey(), e);
        }
        resourceMapper.deleteById(resourceId);
    }

    public ResourcePO getById(Long resourceId) {
        return resourceMapper.selectById(resourceId);
    }

    /**
     * Adjust the bizType of the resource (for example: preview DOCUMENT_PREVIEW → post-commit DOCUMENT).
     */
    public void updateBizType(Long resourceId, String bizType) {
        ResourcePO resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            return;
        }
        resource.setBizType(bizType);
        resourceMapper.updateById(resource);
    }

    public ResourcePO requireResource(Long resourceId) {
        ResourcePO r = resourceMapper.selectById(resourceId);
        if (r == null) throw new AnikAiException("Resource not found: " + resourceId);
        return r;
    }
}

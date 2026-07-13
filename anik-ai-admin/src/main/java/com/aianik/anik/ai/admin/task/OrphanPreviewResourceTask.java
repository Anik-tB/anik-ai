package com.aianik.anik.ai.admin.task;

import com.aianik.anik.ai.features.resource.ResourceService;
import com.aianik.anik.ai.features.resource.enums.ResourceBizTypeEnum;
import com.aianik.anik.ai.persistence.resource.mapper.ResourceMapper;
import com.aianik.anik.ai.persistence.resource.po.ResourcePO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Knowledge base upload preview orphan resource cleanup task
 * <p>
 * When the user opens the upload preview but does not commit or cancel (the page is closed/the network is interrupted),
 * Tokens in Redis naturally expire after 30 minutes, but temporary resources will become orphans.
 * This task scans once an hour and completely cleans up resources with bizType=DOCUMENT_PREVIEW and more than 1 hour old.
 *
 * @author openanik
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanPreviewResourceTask {

    /** Resources that have survived for at least 1 hour and are still in the PREVIEW state are considered orphans (more than twice the Redis token 30min TTL) */
    private static final long ORPHAN_THRESHOLD_MINUTES = 60L;

    /** The batch limit for a single scan is set to prevent pulling too many at once when there is a backlog */
    private static final int BATCH_LIMIT = 200;

    private final ResourceMapper resourceMapper;
    private final ResourceService resourceService;

    /** Hourly hour + 5 minutes (peak tasks staggered on the hour) */
    @Scheduled(cron = "0 5 * * * *")
    public void cleanupOrphanPreviewResources() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(ORPHAN_THRESHOLD_MINUTES);
            List<ResourcePO> orphans = resourceMapper.selectList(
                    new LambdaQueryWrapper<ResourcePO>()
                            .eq(ResourcePO::getBizType, ResourceBizTypeEnum.DOCUMENT_PREVIEW.getValue())
                            .lt(ResourcePO::getCreateDt, threshold)
                            .last("LIMIT " + BATCH_LIMIT));
            if (orphans.isEmpty()) {
                return;
            }
            int ok = 0;
            for (ResourcePO r : orphans) {
                try {
                    resourceService.delete(r.getId());
                    ok++;
                } catch (Exception e) {
                    log.warn("Failed to clean up orphan preview resources: id={}", r.getId(), e);
                }
            }
            log.info("Cleaning up orphan preview resources completed: {} items in total, {} items successful", orphans.size(), ok);
        } catch (Exception e) {
            log.warn("Orphan preview resource cleanup task failed to execute", e);
        }
    }
}

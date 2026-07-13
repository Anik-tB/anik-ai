package com.aianik.anik.ai.admin.task;

import com.aianik.anik.ai.common.enums.memory.MemoryStatusEnum;
import com.aianik.anik.ai.persistence.memory.mapper.ConversationMemoryMapper;
import com.aianik.anik.ai.persistence.memory.po.ConversationMemoryPO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Remember expired scheduled tasks
 * Expired memories are scanned and archived every day at 2:30 AM (status → ARCHIVED)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryExpirationTask {

    private final ConversationMemoryMapper memoryMapper;

    @Scheduled(cron = "0 30 2 * * *")
    public void archiveExpiredMemories() {
        try {
            int rows = memoryMapper.update(null,
                    new LambdaUpdateWrapper<ConversationMemoryPO>()
                            .lt(ConversationMemoryPO::getExpiresAt, LocalDateTime.now())
                            .eq(ConversationMemoryPO::getStatus, MemoryStatusEnum.ACTIVE)
                            .set(ConversationMemoryPO::getStatus, MemoryStatusEnum.ARCHIVED));
            if (rows > 0) {
                log.info("Memory expiration archives: {} archives in total", rows);
            }
        } catch (Exception e) {
            log.warn("Execution of memory expired archiving task failed", e);
        }
    }
}

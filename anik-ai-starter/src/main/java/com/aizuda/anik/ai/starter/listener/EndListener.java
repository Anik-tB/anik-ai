package com.aizuda.anik.ai.starter.listener;

import com.aizuda.anik.ai.common.Lifecycle;
import com.aizuda.anik.ai.common.log.AnikAiLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Close listener
 *
 * @author: openanik
 * @date : 2021-11-19 19:00
 */
@Component
@RequiredArgsConstructor
public class EndListener implements ApplicationListener<ContextClosedEvent> {
    private final List<Lifecycle> lifecycleList;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        AnikAiLog.LOCAL.info("anik-ai client about to shutdown v{}", 1);
        lifecycleList.forEach(Lifecycle::close);
        AnikAiLog.LOCAL.info("anik-ai client closed successfully v{}", 1);
    }
}

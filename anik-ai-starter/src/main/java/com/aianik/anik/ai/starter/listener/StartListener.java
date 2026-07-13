package com.aianik.anik.ai.starter.listener;

import com.aianik.anik.ai.common.Lifecycle;
import com.aianik.anik.ai.common.constants.SystemConstants;
import com.aianik.anik.ai.common.log.AnikAiLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import com.aianik.anik.ai.common.util.AnikAiVersion;

import java.util.List;

/**
 * system starts listener
 *
 * @author: openanik
 * @date : 2021-11-19 19:00
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartListener implements ApplicationListener<ContextRefreshedEvent> {
    private final List<Lifecycle> lifecycleList;
    private volatile boolean isStarted = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (isStarted) {
            AnikAiLog.LOCAL.info("anik-ai server already started v{}", 1);
            return;
        }
        System.out.println(MessageFormatter.format(SystemConstants.LOGO, AnikAiVersion.getVersion()).getMessage());
        AnikAiLog.LOCAL.info("anik-job server is preparing to start... v{}", AnikAiVersion.getVersion());
        lifecycleList.forEach(Lifecycle::start);
        AnikAiLog.LOCAL.info("anik-job server started successfully v{}", AnikAiVersion.getVersion());
        isStarted = true;
    }
}

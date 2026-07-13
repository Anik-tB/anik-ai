package com.aianik.anik.ai.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>
 *
 * </p>
 *
 * @author openanik
 * @date 2025-07-20
 */
@Configuration
public class AsyncConfig  {

    @Bean(name = "anikAiAsyncExecutor")
    @Primary
    public Executor anikAiAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(15);              //Number of core threads
        executor.setMaxPoolSize(20);              //Maximum number of threads
        executor.setQueueCapacity(100);           //queue capacity
        executor.setKeepAliveSeconds(60);         //Thread idle time
        executor.setThreadNamePrefix("async-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}

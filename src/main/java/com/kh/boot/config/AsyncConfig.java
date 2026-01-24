package com.kh.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for thread pool management and security context
 * propagation.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Standard task executor for general async tasks.
     * Optimized for 2C2G server.
     */
    @Bean(name = "khAsyncExecutor")
    public Executor khAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core threads: Number of CPUs
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // Max threads: 2 * CPUs + 1 (prevent excessive context switching on limited
        // CPU)
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
        // Queue capacity: large enough to buffer bursts but not consume all RAM
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("KhAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        // Wrap with security context to propagate authentication to async threads
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}

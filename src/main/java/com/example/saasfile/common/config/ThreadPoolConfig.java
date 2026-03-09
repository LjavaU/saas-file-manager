package com.example.saasfile.common.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@ConfigurationProperties(prefix = "threadpool")
@Getter
public class ThreadPoolConfig {

    private final PoolProperties fileProcessing = new PoolProperties();
    private final PoolProperties fileUpload = new PoolProperties();

    
    @Data
    public static class PoolProperties {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private int keepAliveSeconds;
        private String threadNamePrefix;

    }

    
    @Bean("fileProcessingTaskExecutor")
    public ThreadPoolTaskExecutor fileProcessingTaskExecutor() {
        return createExecutor(fileProcessing);
    }

    
    @Bean("fileUploadTaskExecutor")
    public ThreadPoolTaskExecutor fileUploadTaskExecutor() {
        return createExecutor(fileUpload);
    }

    
    private ThreadPoolTaskExecutor createExecutor(PoolProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}
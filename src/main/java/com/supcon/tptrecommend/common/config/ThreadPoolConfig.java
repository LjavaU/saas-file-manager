package com.supcon.tptrecommend.common.config;

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

    /**
     *用于保存线程池的公共属性的嵌套静态类。
     */
    @Data
    public static class PoolProperties {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private int keepAliveSeconds;
        private String threadNamePrefix;

    }

    /**
     * 为文件处理任务创建线程池。
     * Bean 名为“fileProcessingTaskExecutor”。
     * @return 用于文件处理的 ThreadPoolTaskExecutor 实例。
     */
    @Bean("fileProcessingTaskExecutor")
    public ThreadPoolTaskExecutor fileProcessingTaskExecutor() {
        return createExecutor(fileProcessing);
    }

    /**
     * 为文件上传任务创建线程池。
     * Bean 名为“fileUploadTaskExecutor”。
     * @return ThreadPoolTaskExecutor 实例用于文件上传。
     */
    @Bean("fileUploadTaskExecutor")
    public ThreadPoolTaskExecutor fileUploadTaskExecutor() {
        return createExecutor(fileUpload);
    }

    /**
     * 用于创建和配置 ThreadPoolTaskExecutor 的帮助程序方法。
     *
     * @param properties 线程池的配置属性。
     * @return 配置的 ThreadPoolTaskExecutor。
     */
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
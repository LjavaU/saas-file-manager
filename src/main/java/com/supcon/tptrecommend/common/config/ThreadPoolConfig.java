package com.supcon.tptrecommend.common.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync // 如果你准备结合 @Async 注解使用，最好开启它
public class ThreadPoolConfig {
    /**
     * 定义文件处理的专属线程池
     *
     * @param properties 从yml文件中注入的配置属性
     * @return Executor Bean
     */
    @Bean("fileProcessingExecutor")
    public Executor fileProcessingExecutor(ThreadPoolProperties properties) {
        return new ThreadPoolExecutor(
            properties.getCorePoolSize(),
            properties.getMaxPoolSize(),
            properties.getKeepAliveSeconds(),
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(properties.getQueueCapacity()),
            new ThreadFactoryBuilder().setNameFormat(properties.getThreadNamePrefix() + "%d").build(),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
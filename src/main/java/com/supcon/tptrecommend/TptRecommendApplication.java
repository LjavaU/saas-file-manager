package com.supcon.tptrecommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TptRecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(TptRecommendApplication.class, args);
    }
}

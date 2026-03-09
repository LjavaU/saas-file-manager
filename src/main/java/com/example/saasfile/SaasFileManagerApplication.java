package com.example.saasfile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SaasFileManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaasFileManagerApplication.class, args);
    }
}

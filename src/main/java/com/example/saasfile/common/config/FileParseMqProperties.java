package com.example.saasfile.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mq")
public class FileParseMqProperties {

    private String fileParseBinding = "saas.file.parse";
}
package com.supcon.tptrecommend.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "tpt.mq")
public class FileParseMqProperties {

    private String fileParseBinding = "tpt.recommend.file.parse";
}
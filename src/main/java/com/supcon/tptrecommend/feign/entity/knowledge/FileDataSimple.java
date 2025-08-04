package com.supcon.tptrecommend.feign.entity.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class FileDataSimple {
    /**
     * 文件解析状态
     */
    private String status;
    /**
     * 文件名
     */
    private String file_name;
    /**
     * 关键词
     */
    private List<String> key_words;



}
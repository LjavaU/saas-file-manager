package com.example.saasfile.feign.entity.knowledge;

import lombok.Data;

@Data
public class KnowledgeFileUploadResp<T> {
    private int code;
    private String msg;
    private T data;


}

package com.example.saasfile.feign.entity.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeRecommendationReq {

    
    private List<String> key_words;

    
    private String user_id;

    
    private String bucket;

    
    private String object;

    
    private String tenant_id;
}

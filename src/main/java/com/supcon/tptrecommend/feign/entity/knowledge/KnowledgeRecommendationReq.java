package com.supcon.tptrecommend.feign.entity.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeRecommendationReq {

    /**
     * 关键词
     */
    private List<String> key_words;

    /**
     * 用户 ID
     */
    private String user_id;

    /**
     * 桶
     */
    private String bucket;

    /**
     * 文件全路径
     */
    private String object;

    /**
     * 租户 ID
     */
    private String tenant_id;
}

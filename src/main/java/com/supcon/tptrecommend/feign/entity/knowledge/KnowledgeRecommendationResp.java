package com.supcon.tptrecommend.feign.entity.knowledge;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeRecommendationResp {

    /**
     * 问题
     */
    private List<String> questions;
}

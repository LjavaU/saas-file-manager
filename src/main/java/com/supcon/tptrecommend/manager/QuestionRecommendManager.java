package com.supcon.tptrecommend.manager;

import com.supcon.tptrecommend.dto.questionrecommend.HomeQuestionRecommendReq;

import java.util.List;
import java.util.Set;

public interface QuestionRecommendManager {

    /**
     * 根据用户名刷新首页推荐问题
     *
     * @return {@link List }<{@link String }>
     * @author luhao
     * @date 2025/05/22 13:48:15
     */
    Set<String> refreshHomepageRecommendations(HomeQuestionRecommendReq req);
}

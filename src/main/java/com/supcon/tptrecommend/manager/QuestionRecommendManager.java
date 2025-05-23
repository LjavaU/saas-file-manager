package com.supcon.tptrecommend.manager;

import java.util.List;

public interface QuestionRecommendManager {

    /**
     * 根据用户名刷新首页推荐问题
     *
     * @return {@link List }<{@link String }>
     * @author luhao
     * @date 2025/05/22 13:48:15
     */
    List<String> refreshHomepageRecommendations();
}

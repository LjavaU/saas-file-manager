package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.systemmanagerapi.dto.LoginInfoUserDTO;
import com.supcon.tptrecommend.common.utils.LoginUserUtils;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.entity.UserInfo;
import com.supcon.tptrecommend.manager.QuestionRecommendManager;
import com.supcon.tptrecommend.service.IQuestionService;
import com.supcon.tptrecommend.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionRecommendManagerImpl implements QuestionRecommendManager {

    private final IUserInfoService userInfoService;

    private final IQuestionService questionService;

    /**
     * 根据用户名刷新首页推荐问题
     *
     * @return {@link List }<{@link String }>
     * @author luhao
     * @date 2025/05/22 13:47:54
     */
    @Override
    public List<String> refreshHomepageRecommendations() {

        LoginInfoUserDTO loginUserInfo = LoginUserUtils.getLoginUserInfo();
        // 根据用户名查询岗位、行业和装置信息
        List<UserInfo> userInfos = userInfoService.list(Wrappers.<UserInfo>lambdaQuery()
            .eq(UserInfo::getUserName, loginUserInfo.getUsername())
            .eq(ObjectUtil.isNotNull(loginUserInfo.getId()), UserInfo::getId, loginUserInfo.getId()));
        Optional<UserInfo> userInfoOptional = userInfos.stream().findFirst();
        if (userInfoOptional.isPresent()) {
            UserInfo userInfo = userInfoOptional.get();
            // 根据岗位、行业和装置信息查询内置问题
            List<Question> questions = questionService.list(Wrappers.<Question>lambdaQuery()
                .eq(Question::getPost, userInfo.getPost())
                .eq(Question::getIndustry, userInfo.getIndustry())
                .eq(Question::getDevice, userInfo.getDevice()));
            // 从这些问题集中随机选择一些问题，作为首页推荐
            return getRandomUniqueValueElements(questions, 6);
        }
        return Collections.emptyList();
    }

    /**
     * 获取随机唯一值元素
     *
     * @param questions 问题
     * @param count     计数
     * @return {@link List }<{@link String }>
     * @author luhao
     * @date 2025/05/22 13:47:51
     */
    public static List<String> getRandomUniqueValueElements(List<Question> questions, int count) {
        if (questions == null || questions.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }

        // 1. 去重并转换为列表
        List<String> uniqueElementsList = questions.stream()
            .map(Question::getContent)
            .distinct() // 去除重复值
            .collect(Collectors.toList());

        // 2. 检查是否有足够的不重复元素
        if (uniqueElementsList.size() <= count) {
            Collections.shuffle(uniqueElementsList); // 打乱顺序
            return uniqueElementsList;
        }

        // 3. 打乱并选择
        Collections.shuffle(uniqueElementsList);
        return uniqueElementsList.stream()
            .limit(count) // 取前 count 个
            .collect(Collectors.toList());



    }
}

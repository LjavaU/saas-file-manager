package com.supcon.tptrecommend.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.supcon.tptrecommend.dto.questionrecommend.HomeQuestionRecommendReq;
import com.supcon.tptrecommend.entity.Question;
import com.supcon.tptrecommend.manager.QuestionRecommendManager;
import com.supcon.tptrecommend.mapper.QuestionMapper;
import com.supcon.tptrecommend.service.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionRecommendManagerImpl implements QuestionRecommendManager {


    private final IQuestionService questionService;

    private final QuestionMapper questionMapper;

    /**
     * 根据用户名刷新首页推荐问题
     *
     * @return {@link List }<{@link String }>
     * @author luhao
     * @date 2025/05/22 13:47:54
     */
    public Set<String> refreshHomepageRecommendations(HomeQuestionRecommendReq req) {
        // 根据租户id去查 先查找共性问题
        List<Question> questionByTenant = questionMapper.listQuestionCommon();

        List<Question> questionByIndustry = Collections.emptyList();
        if (StrUtil.isNotBlank(req.getIndustry())) {
            // 根据行业去查问题
            questionByIndustry = questionService.list(Wrappers.<Question>lambdaQuery()
                .eq(Question::getIndustry, req.getIndustry()));
        }
        List<Question> questionByDevice = Collections.emptyList();
        if (StrUtil.isNotBlank(req.getIndustry()) && StrUtil.isNotBlank(req.getDevice())) {
            // 根据行业、装置去查问题
            questionByDevice = questionService.list(Wrappers.<Question>lambdaQuery()
                .eq(Question::getIndustry, req.getIndustry())
                .eq(Question::getDevice, req.getDevice()));
        }
        List<Question> questionByPost = Collections.emptyList();
        if (StrUtil.isNotBlank(req.getIndustry()) && StrUtil.isNotBlank(req.getDevice()) && StrUtil.isNotBlank(req.getPost())) {
            // 根据行业、装置、岗位去查问题
            questionByPost = questionService.list(Wrappers.<Question>lambdaQuery()
                .eq(Question::getIndustry, req.getIndustry())
                .eq(Question::getDevice, req.getDevice())
                .eq(Question::getPost, req.getPost()));
        }
        // 最终的问题推荐结果集
        Set<String> results = Sets.newHashSet();
        // 按照通用、行业、装置、岗位 层级 依次获取2个问题
        List<String> remainsByTenant = Lists.newArrayList();
        getRandomUniqueQuestions(questionByTenant, results, 2, remainsByTenant);
        List<String> remainsByIndustry = Lists.newArrayList();
        getRandomUniqueQuestions(questionByIndustry, results, 2, remainsByIndustry);
        List<String> remainsByDevice = Lists.newArrayList();
        getRandomUniqueQuestions(questionByDevice, results, 2, remainsByDevice);
        List<String> remainsByPost = Lists.newArrayList();
        getRandomUniqueQuestions(questionByPost, results, 2, remainsByPost);
        // 从剩余问题集中随机获取剩余条目的问题
        List<String> combinedRemain = Lists.newArrayList(remainsByTenant, remainsByIndustry, remainsByDevice, remainsByPost).stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        Collections.shuffle(combinedRemain);
        int remainResultCount = 10 - results.size();
        for (String ques : combinedRemain) {
            // 判断是否大于阀值
            if (remainResultCount <= 0) {
                break;
            }
            if (results.add(ques)) {
                remainResultCount--;
            }

        }
        return results;
    }

    public static void getRandomUniqueQuestions(List<Question> questions, Set<String> results, int count, List<String> remains) {
        if (CollectionUtil.isEmpty(questions)) {
            return;
        }
        // 1. 去重并转换为列表
        List<String> uniqueElementsList = questions.stream()
            .map(Question::getContent)
            .distinct() // 去除重复值
            .collect(Collectors.toList());

        // 2. 检查是否有足够的不重复元素
        if (uniqueElementsList.size() <= count) {
            Collections.shuffle(uniqueElementsList); // 打乱顺序
            results.addAll(uniqueElementsList);
            return;
        }
        // 打乱顺序
        Collections.shuffle(uniqueElementsList);
        int addCount = 0;
        List<String> alreadyAdd = new ArrayList<>();
        // 3. 获取随机不重复元素
        for (String ques : uniqueElementsList) {
            // 判断是否大于阀值
            if (addCount >= count) {
                break;
            }
            if (results.add(ques)) {
                addCount++;
            }
            // 记录已经添加过的元素
            alreadyAdd.add(ques);
        }
        //  4. 获取剩余元素
        uniqueElementsList.removeAll(alreadyAdd);
        remains.addAll(uniqueElementsList);
    }


}

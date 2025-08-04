package com.supcon.tptrecommend.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.supcon.framework.schedule.core.entity.ExecutorParam;
import com.supcon.framework.schedule.core.handler.AbstractJobHandler;
import com.supcon.tptrecommend.common.enums.KnowledgeParseState;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationReq;
import com.supcon.tptrecommend.dto.filerecommendation.FileRecommendationResp;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.entity.FileRecommendation;
import com.supcon.tptrecommend.feign.KnowledgeFeign;
import com.supcon.tptrecommend.feign.entity.knowledge.FileDataSimple;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeParseDetails;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeRecommendationReq;
import com.supcon.tptrecommend.service.IFileObjectService;
import com.supcon.tptrecommend.service.IFileRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 知识库解析状态任务处理程序
 *
 * @author luhao
 * @since 2025/07/30 09:44:54
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeParseStatusJobHandler extends AbstractJobHandler {

    private final KnowledgeFeign knowledgeFeign;

    private final IFileObjectService fileObjectService;

    private final IFileRecommendationService fileRecommendationService;

    private final Executor JOB_EXECUTOR = new ThreadPoolExecutor(4, 8,
        35000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(30),
        new ThreadPoolExecutor.AbortPolicy());

    /**
     * 具体执行方法
     *
     * @param userParam     用户参数
     * @param executorParam 执行参数
     */
    @Override
    public void execute(Map<String, Object> userParam, ExecutorParam executorParam) {
        List<FileObjectResp> knowledgeParsing = fileObjectService.getKnowledgeParsing();
        if (CollectionUtil.isEmpty(knowledgeParsing)) {
            return;
        }
        for (FileObjectResp fileObjectResp : knowledgeParsing) {
            KnowledgeFileUploadResp<KnowledgeParseDetails> parseDetails = knowledgeFeign.listFiles(String.valueOf(fileObjectResp.getUserId()), fileObjectResp.getBucketName(), fileObjectResp.getObjectName(), fileObjectResp.getTenantId());
            if (Objects.nonNull(parseDetails) && parseDetails.getCode() == HttpStatus.HTTP_OK) {
                KnowledgeParseDetails parseDetailsData = parseDetails.getData();
                if (Objects.nonNull(parseDetailsData)) {
                    List<FileDataSimple> details = parseDetailsData.getDetails();
                    if (CollectionUtil.isNotEmpty(details)) {
                        FileDataSimple fileDataSimple = details.get(0);
                        String status = fileDataSimple.getStatus();
                        // 更新状态
                        updateKnowledgeParseState(fileObjectResp, status);
                        // 如果状态为绿色，则获取推荐问题
                        CompletableFuture.runAsync(() -> {
                            fetchRecommendationsWhenGreen(fileObjectResp, status, fileDataSimple);
                        }, JOB_EXECUTOR).exceptionally(throwable -> {
                            log.error("获取知识库推荐问题失败", throwable);
                            return null;
                        });
                    }
                }

            } else {
                log.error("获取知识库文件解析状态失败：{}", JSONUtil.parse(parseDetails));
            }

        }

    }

    private void updateKnowledgeParseState(FileObjectResp fileObjectResp, String status) {
        FileObject fileObject = new FileObject();
        fileObject.setKnowledgeParseState(KnowledgeParseState.valueByDesc(status));
        fileObject.setId(fileObjectResp.getId());
        fileObject.setTenantId(fileObjectResp.getTenantId());
        fileObject.setUpdateTime(LocalDateTime.now());
        fileObjectService.updateKnowledgeParseState(fileObject);
    }

    private void fetchRecommendationsWhenGreen(FileObjectResp fileObjectResp, String status, FileDataSimple fileDataSimple) {
        if (KnowledgeParseState.GREEN.getValue().equals(KnowledgeParseState.valueByDesc(status))) {
            KnowledgeRecommendationReq req = new KnowledgeRecommendationReq();
            req.setBucket(fileObjectResp.getBucketName());
            req.setObject(fileObjectResp.getObjectName());
            req.setUser_id(String.valueOf(fileObjectResp.getUserId()));
            req.setTenant_id(fileObjectResp.getTenantId());
            // 获取关键词
            FileRecommendationResp recommendationResp = fileRecommendationService.getKeyWord(FileRecommendationReq.builder()
                .fileId(fileObjectResp.getId())
                .tenantId(fileObjectResp.getTenantId()).build());
            String keyword = Optional.ofNullable(recommendationResp).map(FileRecommendationResp::getKeyword)
                .orElse("\"基本原理\", \"发展历程\",\"技术\", \"应用\", \"技术关键点\", \"技术优势\"");
            req.setKey_words(Arrays.asList(keyword.split(",")));
            // 获取推荐问题
            KnowledgeFileUploadResp<List<String>> knowledgeRecommendation = knowledgeFeign.getRecommendation(req);
            if (Objects.nonNull(knowledgeRecommendation) && knowledgeRecommendation.getCode() == HttpStatus.HTTP_OK) {
                List<String> questions = knowledgeRecommendation.getData();
                String questionStr = JSONUtil.toJsonStr(questions);
                if (CollectionUtil.isNotEmpty(questions)) {
                    updateOrSaveFileRecommendation(fileObjectResp, recommendationResp, questionStr);
                }
            }

        }
    }

    private void updateOrSaveFileRecommendation(FileObjectResp fileObjectResp, FileRecommendationResp recommendationResp, String questionStr) {
        if (Objects.nonNull(recommendationResp)) {
            FileRecommendation fileRecommendation = new FileRecommendation();
            fileRecommendation.setId(recommendationResp.getId());
            fileRecommendation.setQuestions(questionStr);
            fileRecommendation.setUpdateTime(LocalDateTime.now());
            fileRecommendationService.updateFileRecommend(fileRecommendation);
        } else {
            FileRecommendation fileRecommendation = new FileRecommendation();
            fileRecommendation.setQuestions(questionStr);
            fileRecommendation.setFileId(fileObjectResp.getId());
            fileRecommendation.setTenantId(fileObjectResp.getTenantId());
            fileRecommendationService.saveFileRecommend(fileRecommendation);


        }
    }

    @Override
    public String name() {
        return "knowledgeParseStatusJob";
    }

    /**
     * 处理消息发生异常时调用，默认不做任何处理
     *
     * @param userParam     用户参数
     * @param executorParam 执行参数
     * @param e             异常
     */
    @Override
    public void handlerException(Map<String, Object> userParam, ExecutorParam executorParam, Exception e) {
        log.error("任务执行异常", e);
    }
}
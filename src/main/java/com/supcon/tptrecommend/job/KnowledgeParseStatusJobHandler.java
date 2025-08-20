package com.supcon.tptrecommend.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.supcon.framework.schedule.core.annotation.Job;
import com.supcon.framework.schedule.core.enums.ScheduleTypeEnum;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.enums.KnowledgeParseState;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
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
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class KnowledgeParseStatusJobHandler  {

    private final KnowledgeFeign knowledgeFeign;

    private final IFileObjectService fileObjectService;

    private final IFileRecommendationService fileRecommendationService;

    private final Executor JOB_EXECUTOR = new ThreadPoolExecutor(20, 40,
        30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500),
        new ThreadPoolExecutor.CallerRunsPolicy());

    @XxlJob("knowledgeParseStatusJob")
    @Job(jobDesc = "定时轮询知识库文件解析状态", scheduleType = ScheduleTypeEnum.FIX_RATE, scheduleConf = "30", alarmEmail = "")
    public void execute() {
        List<FileObjectResp> knowledgeParsing = fileObjectService.getKnowledgeParsing(KnowledgeParseState.GRAY.getValue());
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
                        Integer knowledgeParseState = KnowledgeParseState.valueByDesc(status);
                        if (Objects.nonNull(knowledgeParseState)) {
                            FileObject fileObject = buildFileObject(fileObjectResp, knowledgeParseState);
                            // TODO：知识库的最终解析状态更新文件解析状态
                            if (KnowledgeParseState.GREEN.getValue().equals(knowledgeParseState)) {
                                // 如果状态为绿色，则获取推荐问题
                                fetchRecommendationsWhenGreen(fileObjectResp, status);
                                fileObject.setFileStatus(FileStatus.PARSED.getValue());
                            } else if (KnowledgeParseState.RED.getValue().equals(knowledgeParseState) || KnowledgeParseState.YELLOW.getValue().equals(knowledgeParseState)) {
                                fileObject.setFileStatus(FileStatus.PARSE_FAILED.getValue());
                                ProcessProgressSupport.notifyParseComplete(fileObjectResp.getId(),fileObjectResp.getUserId());

                            } else {
                                fileObject.setFileStatus(FileStatus.UNPARSED.getValue());
                                ProcessProgressSupport.notifyParseProcessing(fileObjectResp.getId(),fileObjectResp.getUserId(),50);
                            }
                            // 更新状态
                            fileObjectService.updateKnowledgeParseState(fileObject);
                        }


                    }
                }

            } else {
                log.error("获取知识库文件解析状态失败：{}", JSONUtil.parse(parseDetails));
            }

        }

    }


    @NotNull
    private static FileObject buildFileObject(FileObjectResp fileObjectResp, Integer knowledgeParseState) {
        FileObject fileObject = new FileObject();
        fileObject.setKnowledgeParseState(knowledgeParseState);
        fileObject.setId(fileObjectResp.getId());
        fileObject.setTenantId(fileObjectResp.getTenantId());
        fileObject.setUpdateTime(LocalDateTime.now());
        return fileObject;
    }

    private void fetchRecommendationsWhenGreen(FileObjectResp fileObjectResp, String status) {
        if (KnowledgeParseState.GREEN.getValue().equals(KnowledgeParseState.valueByDesc(status))) {
            Long userId = fileObjectResp.getUserId();
            Long fileId = fileObjectResp.getId();
            CompletableFuture.runAsync(() -> {
                KnowledgeRecommendationReq req = new KnowledgeRecommendationReq();
                req.setBucket(fileObjectResp.getBucketName());
                req.setObject(fileObjectResp.getObjectName());
                req.setUser_id(String.valueOf(userId));
                req.setTenant_id(fileObjectResp.getTenantId());
                // 获取关键词
                FileRecommendationResp recommendationResp = fileRecommendationService.getKeyWord(FileRecommendationReq.builder()
                    .fileId(fileId)
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
                    ProcessProgressSupport.notifyParseComplete(fileId, userId);
                }

            }, JOB_EXECUTOR).exceptionally(throwable -> {
                log.error("获取知识库推荐问题失败", throwable);
                ProcessProgressSupport.notifyParseComplete(fileId, userId);
                return null;
            });

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

}
package com.example.saasfile.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.example.saasfile.support.schedule.Job;
import com.example.saasfile.support.schedule.ScheduleTypeEnum;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.enums.KnowledgeParseState;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.dto.fileobject.FileObjectResp;
import com.example.saasfile.dto.filerecommendation.FileRecommendationReq;
import com.example.saasfile.dto.filerecommendation.FileRecommendationResp;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.entity.FileRecommendation;
import com.example.saasfile.feign.KnowledgeFeign;
import com.example.saasfile.feign.entity.knowledge.FileDataSimple;
import com.example.saasfile.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.example.saasfile.feign.entity.knowledge.KnowledgeParseDetails;
import com.example.saasfile.feign.entity.knowledge.KnowledgeRecommendationReq;
import com.example.saasfile.service.IFileObjectService;
import com.example.saasfile.service.IFileRecommendationService;
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
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeParseStatusJobHandler {

    private final KnowledgeFeign knowledgeFeign;
    private final IFileObjectService fileObjectService;
    private final IFileRecommendationService fileRecommendationService;

    private final Executor jobExecutor = new ThreadPoolExecutor(
        20,
        40,
        30L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(500),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @XxlJob("knowledgeParseStatusJob")
    @Job(jobDesc = "Poll knowledge parse status", scheduleType = ScheduleTypeEnum.FIX_RATE, scheduleConf = "30", alarmEmail = "")
    public void execute() {
        List<FileObjectResp> knowledgeParsing = fileObjectService.getKnowledgeParsing(KnowledgeParseState.GRAY.getValue());
        if (CollectionUtil.isEmpty(knowledgeParsing)) {
            return;
        }

        for (FileObjectResp fileObjectResp : knowledgeParsing) {
            KnowledgeFileUploadResp<KnowledgeParseDetails> parseDetails =
                knowledgeFeign.listFiles(
                    String.valueOf(fileObjectResp.getUserId()),
                    fileObjectResp.getBucketName(),
                    fileObjectResp.getObjectName(),
                    fileObjectResp.getTenantId()
                );

            if (Objects.nonNull(parseDetails) && parseDetails.getCode() == HttpStatus.HTTP_OK) {
                KnowledgeParseDetails parseDetailsData = parseDetails.getData();
                if (Objects.nonNull(parseDetailsData) && CollectionUtil.isNotEmpty(parseDetailsData.getDetails())) {
                    FileDataSimple fileDataSimple = parseDetailsData.getDetails().get(0);
                    Integer knowledgeParseState = KnowledgeParseState.valueByDesc(fileDataSimple.getStatus());
                    if (Objects.nonNull(knowledgeParseState)) {
                        FileObject fileObject = buildFileObject(fileObjectResp, knowledgeParseState);
                        if (KnowledgeParseState.GREEN.getValue().equals(knowledgeParseState)) {
                            fetchRecommendationsWhenGreen(fileObjectResp, fileDataSimple.getStatus());
                            fileObject.setFileStatus(FileStatus.PARSED.getValue());
                        } else if (KnowledgeParseState.RED.getValue().equals(knowledgeParseState)
                            || KnowledgeParseState.YELLOW.getValue().equals(knowledgeParseState)) {
                            fileObject.setFileStatus(FileStatus.PARSE_FAILED.getValue());
                            ProcessProgressSupport.notifyParseComplete(fileObjectResp.getId(), fileObjectResp.getUserId());
                        } else {
                            fileObject.setFileStatus(FileStatus.UNPARSED.getValue());
                            ProcessProgressSupport.notifyParseProcessing(fileObjectResp.getId(), fileObjectResp.getUserId(), 50);
                        }
                        fileObjectService.updateKnowledgeParseState(fileObject);
                    }
                }
            } else {
                log.error("Failed to query knowledge parse status: {}", JSONUtil.toJsonStr(parseDetails));
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
        if (!KnowledgeParseState.GREEN.getValue().equals(KnowledgeParseState.valueByDesc(status))) {
            return;
        }

        Long userId = fileObjectResp.getUserId();
        Long fileId = fileObjectResp.getId();
        jobExecutor.execute(() -> {
            try {
                KnowledgeRecommendationReq req = new KnowledgeRecommendationReq();
                req.setBucket(fileObjectResp.getBucketName());
                req.setObject(fileObjectResp.getObjectName());
                req.setUser_id(String.valueOf(userId));
                req.setTenant_id(fileObjectResp.getTenantId());

                FileRecommendationResp recommendationResp = fileRecommendationService.getKeyWord(
                    FileRecommendationReq.builder()
                        .fileId(fileId)
                        .tenantId(fileObjectResp.getTenantId())
                        .build()
                );

                String keyword = Optional.ofNullable(recommendationResp)
                    .map(FileRecommendationResp::getKeyword)
                    .orElse("\"keyword1\",\"keyword2\",\"keyword3\"");
                req.setKey_words(Arrays.asList(keyword.split(",")));

                KnowledgeFileUploadResp<List<String>> knowledgeRecommendation = knowledgeFeign.getRecommendation(req);
                if (Objects.nonNull(knowledgeRecommendation) && knowledgeRecommendation.getCode() == HttpStatus.HTTP_OK) {
                    List<String> questions = knowledgeRecommendation.getData();
                    if (CollectionUtil.isNotEmpty(questions)) {
                        updateOrSaveFileRecommendation(fileObjectResp, recommendationResp, JSONUtil.toJsonStr(questions));
                    }
                    ProcessProgressSupport.notifyParseComplete(fileId, userId);
                }
            } catch (Exception throwable) {
                log.error("Failed to fetch knowledge recommendations", throwable);
                ProcessProgressSupport.notifyParseComplete(fileId, userId);
            }
        });
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

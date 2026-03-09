package com.example.saasfile.manager.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import com.example.saasfile.support.tenant.TenantContext;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.enums.KnowledgeParseState;
import com.example.saasfile.common.utils.FileUtils;
import com.example.saasfile.common.utils.MinioUtils;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.common.utils.RandomUtil;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.entity.FileRecommendation;
import com.example.saasfile.feign.entity.knowledge.FileDataSimple;
import com.example.saasfile.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.example.saasfile.service.IFileObjectService;
import com.example.saasfile.service.IFileRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeFileHandleTemplate {

    @Value("${service.knowledge.address:localhost}")
    private String knowledgeUploadUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final IFileObjectService fileObjectService;
    private final IFileRecommendationService fileRecommendationService;
    private final MinioUtils minioUtils;

    private final Executor knowledgeExecutor = new ThreadPoolExecutor(
        20,
        40,
        30L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(500),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public void uploadToKnowledgeBase(Long fileId, String objectName, String bucketName, Long fileSize, Long userId) {
        String tenantId = TenantContext.getCurrentTenant();
        CompletableFuture.runAsync(() -> {
            TenantContext.setCurrentTenant(tenantId);
            try {
            KnowledgeFileUploadResp<List<FileDataSimple>> resp = uploadFileUploadToKnowledge(objectName, bucketName, fileSize, userId);
            if (Objects.nonNull(resp) && (resp.getCode() == HttpStatus.HTTP_OK || CollectionUtil.isNotEmpty(resp.getData()))) {
                ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(15, 20));
                FileDataSimple fileDataSimple = resp.getData().get(0);
                saveFileKeywordsToRecommendation(fileId, fileDataSimple.getKey_words());
                updateKnowledgeParseState(fileId, fileDataSimple.getStatus());
            } else {
                log.error("Upload to knowledge base failed: {}", Objects.nonNull(resp) ? resp.getMsg() : "");
                markKnowledgeFileAsParseFailed(fileId, userId);
            }
            } finally {
                TenantContext.clear();
            }
        }, knowledgeExecutor).exceptionally(throwable -> {
            log.error("Upload to knowledge base failed", throwable);
            markKnowledgeFileAsParseFailed(fileId, userId);
            return null;
        });
    }

    private KnowledgeFileUploadResp<List<FileDataSimple>> uploadFileUploadToKnowledge(String objectName, String bucketName, Long fileSize, Long userId) {
        String url = knowledgeUploadUrl + "/api/industry_domain_qa/saas/new/upload_files";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = getMultiValueMapHttpEntity(objectName, bucketName, fileSize, headers, userId);

        ParameterizedTypeReference<KnowledgeFileUploadResp<List<FileDataSimple>>> responseType =
            new ParameterizedTypeReference<KnowledgeFileUploadResp<List<FileDataSimple>>>() {
            };

        ResponseEntity<KnowledgeFileUploadResp<List<FileDataSimple>>> responseEntity = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            responseType
        );
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }
        return null;
    }

    private HttpEntity<MultiValueMap<String, Object>> getMultiValueMapHttpEntity(String objectName, String bucketName, Long fileSize, HttpHeaders headers, Long userId) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        InputStreamResource resource = new InputStreamResource(minioUtils.getFileInputStream(bucketName, objectName)) {
            @Override
            public String getFilename() {
                return FileUtils.getFileNameFromObjectKey(objectName);
            }

            @Override
            public long contentLength() {
                return fileSize;
            }
        };
        body.add("files", resource);
        body.add("user_id", userId);
        body.add("bucket", bucketName);
        body.add("object", objectName);
        body.add("tenant_id", TenantContext.getCurrentTenant());
        return new HttpEntity<>(body, headers);
    }

    private void updateKnowledgeParseState(Long fileId, String status) {
        FileObject fileObject = new FileObject();
        fileObject.setKnowledgeParseState(KnowledgeParseState.valueByDesc(status));
        fileObject.setId(fileId);
        fileObjectService.updateById(fileObject);
    }

    private void saveFileKeywordsToRecommendation(Long fileId, List<String> keyWords) {
        if (CollectionUtil.isNotEmpty(keyWords)) {
            FileRecommendation fileRecommendation = new FileRecommendation();
            fileRecommendation.setFileId(fileId);
            fileRecommendation.setKeyword(String.join(",", keyWords));
            fileRecommendationService.save(fileRecommendation);
        }
    }

    private void markKnowledgeFileAsParseFailed(Long fileId, Long userId) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(FileStatus.PARSE_FAILED.getValue());
        fileObject.setKnowledgeParseState(KnowledgeParseState.RED.getValue());
        fileObjectService.updateById(fileObject);
        ProcessProgressSupport.notifyParseComplete(fileId, userId);
    }
}

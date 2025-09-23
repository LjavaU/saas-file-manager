package com.supcon.tptrecommend.manager.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.alibaba.ttl.TtlRunnable;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.enums.KnowledgeParseState;
import com.supcon.tptrecommend.common.utils.FileUtils;
import com.supcon.tptrecommend.common.utils.MinioUtils;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.common.utils.RandomUtil;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.entity.FileRecommendation;
import com.supcon.tptrecommend.feign.entity.knowledge.FileDataSimple;
import com.supcon.tptrecommend.feign.entity.knowledge.KnowledgeFileUploadResp;
import com.supcon.tptrecommend.service.IFileObjectService;
import com.supcon.tptrecommend.service.IFileRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

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

    /**
     * 知识库解析线程池
     */
    private final Executor KNOWLEDGE_EXECUTOR = new ThreadPoolExecutor(20, 40,
        30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500),
        new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 上传到知识库
     *
     * @param fileId     文件 ID
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @param fileSize   文件大小
     * @param userId     用户 ID
     * @author luhao
     * @since 2025/08/08 13:17:42
     *
     *
     */
    public void uploadToKnowledgeBase(Long fileId,String objectName, String bucketName, Long fileSize,Long userId) {
        CompletableFuture.runAsync(TtlRunnable.get(() -> {
            log.warn("uploadToKnowledgeBase - 当前租户: {}", TenantContext.getCurrentTenant());
            KnowledgeFileUploadResp<List<FileDataSimple>> resp = uploadFileUploadToKnowledge(objectName, bucketName,fileSize, userId);
            if (Objects.nonNull(resp) && (resp.getCode() == HttpStatus.HTTP_OK || CollectionUtil.isNotEmpty(resp.getData()))) {
                log.warn("上传文件到知识库成功，fileId：{}，resp：{}", fileId, JSONUtil.toJsonStr(resp));
                // 通知解析进度
                ProcessProgressSupport.notifyParseProcessing(fileId,userId, RandomUtil.getRandomPercentage(15, 20) );
                FileDataSimple fileDataSimple = resp.getData().get(0);
                List<String> keyWords = fileDataSimple.getKey_words();
                log.warn("文件：{}，关键词：{}", fileId, JSONUtil.toJsonStr(keyWords));
                // 保存文件关键词到文件推荐表中
                saveFileKeywordsToRecommendation(fileId, keyWords);
                log.warn("开始更新文件解析状态，fileId：{}，status：{}", fileId, fileDataSimple.getStatus());
                // 更新文件解析状态
                updateKnowledgeParseState(fileId, fileDataSimple.getStatus());
                log.warn("更新文件解析状态成功，fileId：{}", fileId);
            } else {
                log.error("上传文件到知识库失败：{}", Objects.nonNull(resp) ? resp.getMsg() : "");
                markKnowledgeFileAsParseFailed(fileId, userId);
            }
        }), KNOWLEDGE_EXECUTOR).exceptionally(throwable -> {
            log.error("上传文件到知识库失败", throwable);
            markKnowledgeFileAsParseFailed(fileId, userId);
            return null;
        });


    }

    private KnowledgeFileUploadResp<List<FileDataSimple>> uploadFileUploadToKnowledge(String objectName, String bucketName, Long fileSize, Long userId) {
        String url = knowledgeUploadUrl + "/api/industry_domain_qa/saas/new/upload_files";
        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 2. 创建 MultiValueMap 来构建请求体
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
        // 3. 将 File 包装成 FileSystemResource
        body.add("files", resource);
        body.add("user_id", userId);
        body.add("bucket", bucketName);
        body.add("object", objectName);
        body.add("tenant_id", TenantContext.getCurrentTenant());

        // 4. 将请求头和请求体封装到 HttpEntity 中
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

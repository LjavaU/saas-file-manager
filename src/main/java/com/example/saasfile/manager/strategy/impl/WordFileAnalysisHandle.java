package com.example.saasfile.manager.strategy.impl;

import com.example.saasfile.support.exception.ServerException;
import com.example.saasfile.common.enums.FileCategory;
import com.example.saasfile.common.enums.FileCategoryAbilityAssociation;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.enums.TagHistoryCategory;
import com.example.saasfile.common.utils.FileUtils;
import com.example.saasfile.common.utils.MinioUtils;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.common.utils.RandomUtil;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.feign.LlmFeign;
import com.example.saasfile.feign.entity.llm.FileClassifyReq;
import com.example.saasfile.feign.entity.llm.FileClassifyResp;
import com.example.saasfile.manager.strategy.FileAnalysisHandle;
import com.example.saasfile.manager.strategy.KnowledgeFileHandleTemplate;
import com.example.saasfile.service.IFileObjectService;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordFileAnalysisHandle implements FileAnalysisHandle {

    private final RestTemplate restTemplate = new RestTemplate();
    private final IFileObjectService fileObjectService;
    private final LlmFeign llmFeign;
    private final KnowledgeFileHandleTemplate knowledgeFileHandleTemplate;

    @Value("${service.llm.address:localhost}")
    private String llmUrl;

    private final MinioUtils minioUtils;

    @Override
    public void handleFileAnalysis(Long fileId, Integer category) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (Objects.isNull(fileObject)) {
            log.error("Word analysis skipped because file record does not exist: {}", fileId);
            return;
        }

        Long userId = fileObject.getUserId();
        ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(5, 10));
        knowledgeFileHandleTemplate.uploadToKnowledgeBase(fileId, fileObject.getObjectName(), fileObject.getBucketName(), fileObject.getFileSize(), userId);

        String objectName = fileObject.getObjectName();
        String uniqueFilename = FileUtils.getFileNameFromObjectKey(objectName);
        String originalName = fileObject.getOriginalName();

        if (fileObject.getFileSize() > 1024 * 1024 * 100) {
            log.error("Word file is too large, parsing aborted");
            return;
        }

        File file = minioUtils.saveStreamToTempFile(fileObject.getBucketName(), objectName, uniqueFilename);
        if (file == null) {
            log.error("Failed to create temporary file for {}", originalName);
            throw new ServerException("Failed to create temporary file: " + originalName);
        }

        ResponseEntity<byte[]> responseEntity;
        try {
            responseEntity = callLlmApiWithFile(file);
        } catch (Exception e) {
            log.error("Call to LLM convert API failed", e);
            return;
        } finally {
            FileUtils.deleteTemporaryFile(file, originalName);
        }

        if (responseEntity != null) {
            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                log.error("{} markdown conversion failed", originalName);
                return;
            }
            byte[] fileBytes = responseEntity.getBody();
            Charset charset = getCharsetFromResponse(responseEntity);
            String content = new String(fileBytes, charset);
            analysis(content, fileId, originalName);
        }
    }

    private void analysis(String content, Long fileId, String originalName) {
        int segmentSize = 1024;
        String headMarkdownContent = content.substring(0, Math.min(segmentSize, content.length()));
        FileClassifyResp classifyResp = llmFeign.classify(FileClassifyReq.builder()
            .headMarkdownContent(headMarkdownContent)
            .documentType("doc")
            .build());
        if (Objects.isNull(classifyResp)) {
            log.warn("LLM classify response is empty for {}", originalName);
            throw new ServerException("File classify failed");
        }
        updateFileParseMetadata(
            fileId,
            FileCategory.getValueByCode(classifyResp.getCategory()),
            classifyResp.getSummary(),
            classifyResp.getSubcategory(),
            classifyResp.getThird_level_category()
        );
    }

    private void updateFileParseMetadata(Long fileId, String category, String summary, Integer subcategory, Integer thirdLevelCategory) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setCategory(category);
        fileObject.setContentOverview(summary);
        fileObject.setSubCategory(String.valueOf(subcategory));
        if (thirdLevelCategory != -1) {
            fileObject.setThirdLevelCategory(String.valueOf(thirdLevelCategory));
            fileObject.setAbility(FileCategoryAbilityAssociation.getAbilityByTagHistoryCategory(TagHistoryCategory.getByCode(thirdLevelCategory)));
        } else {
            fileObject.setAbility(FileCategoryAbilityAssociation.getAbilityBySubCategory(SubCategoryEnum.getByCode(subcategory)));
        }
        fileObjectService.updateById(fileObject);
    }

    private Charset getCharsetFromResponse(ResponseEntity<?> responseEntity) {
        MediaType contentType = responseEntity.getHeaders().getContentType();
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }
        return StandardCharsets.UTF_8;
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("doc", "docx");
    }

    public ResponseEntity<byte[]> callLlmApiWithFile(File file) {
        String url = llmUrl + "/api/file/convert";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, requestEntity, byte[].class);
    }
}

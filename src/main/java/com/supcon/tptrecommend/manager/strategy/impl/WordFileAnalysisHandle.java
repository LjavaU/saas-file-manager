package com.supcon.tptrecommend.manager.strategy.impl;

import com.google.common.collect.Sets;
import com.supcon.systemcommon.exception.ServerException;
import com.supcon.tptrecommend.common.enums.FileCategory;
import com.supcon.tptrecommend.common.enums.FileCategoryAbilityAssociation;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.enums.TagHistoryCategory;
import com.supcon.tptrecommend.common.utils.FileUtils;
import com.supcon.tptrecommend.common.utils.MinioUtils;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.LlmFeign;
import com.supcon.tptrecommend.feign.entity.llm.FileClassifyReq;
import com.supcon.tptrecommend.feign.entity.llm.FileClassifyResp;
import com.supcon.tptrecommend.manager.strategy.FileAnalysisHandle;
import com.supcon.tptrecommend.manager.strategy.KnowledgeFileHandleTemplate;
import com.supcon.tptrecommend.service.IFileObjectService;
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
    public void handleFileAnalysis(Long fileId,Integer category) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (Objects.isNull(fileObject)) {
            log.error("文件不存在，解析任务终止");
            return;
        }
        // 上传知识库
        knowledgeFileHandleTemplate.uploadToKnowledgeBase(fileId);
        String objectName = fileObject.getObjectName();
        String uniqueFilename = FileUtils.getFileNameFromObjectName(objectName);
        String originalName = fileObject.getOriginalName();
        // TODO：控制word文件大小
        if (fileObject.getFileSize() > 1024 * 1024 * 100) {
            log.error("word文件过大，解析任务终止");
            return;
        }

        File file = minioUtils.saveStreamToTempFile(fileObject.getBucketName(), objectName, uniqueFilename);
        if (file == null) {
            log.error("创建临时文件失败：{}，解析任务终止", originalName);
            throw new ServerException("临时文件" + originalName + "保存失败");
        }
        ResponseEntity<byte[]> responseEntity;
        try {
            responseEntity = callLlmApiWithFile(file);
        } catch (Exception e) {
            log.error("调用llm接口：api/file/convert访问出错 ", e);
            return;
        } finally {
            FileUtils.deleteTemporaryFile(file, originalName);
        }
        if (responseEntity != null) {
            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
                log.error("{}文件调用py接口转换markdown有误", originalName);
                return;
            }
            byte[] fileBytes = responseEntity.getBody();
            // 2. 确定字符编码
            Charset charset = getCharsetFromResponse(responseEntity);
            // 3. 使用确定的编码将字节数组转换为字符串
            String content = new String(fileBytes, charset);
            analysis(content, fileId, originalName);
        }

    }

    private void analysis(String content, Long fileId, String originalName) {
        final int segmentSize = 1024;
        String headMarkdownContent = content.substring(0, Math.min(segmentSize, content.length()));
        FileClassifyResp classifyResp = llmFeign.classify(FileClassifyReq.builder()
            .headMarkdownContent(headMarkdownContent)
            .documentType("doc")
            .build());
        if (Objects.isNull(classifyResp)) {
            log.warn("文件：{}，LLM分类失败", originalName);
            throw new ServerException("文件分类失败");

        }
        updateFileParseMetadata(fileId, FileCategory.getValueByCode(classifyResp.getCategory()), classifyResp.getSummary(), classifyResp.getSubcategory(), classifyResp.getThird_level_category());
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


    /**
     * 从HTTP响应头中解析Content-Type以获取字符集。
     *
     * @param responseEntity 响应实体
     * @return 解析出的字符集，如果未指定则默认为UTF-8
     */
    private Charset getCharsetFromResponse(ResponseEntity<?> responseEntity) {
        MediaType contentType = responseEntity.getHeaders().getContentType();
        if (contentType != null && contentType.getCharset() != null) {
            return contentType.getCharset();
        }
        // 如果响应头没有指定编码，提供一个安全可靠的默认值
        return StandardCharsets.UTF_8;
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("doc", "docx");
    }


    public ResponseEntity<byte[]> callLlmApiWithFile(File file) {
        String url = llmUrl + "/api/file/convert";
        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        // 指定内容类型为 multipart/form-data
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 构建请求体 (Body)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 将文件包装成 FileSystemResource
        body.add("file", new FileSystemResource(file));

        // 创建完整的HttpEntity
        // HttpEntity 封装了请求头和请求体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.postForEntity(url, requestEntity, byte[].class);


    }
}

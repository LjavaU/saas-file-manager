package com.example.saasfile.manager.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.example.saasfile.support.redis.RedisService;
import com.example.saasfile.support.tenant.TenantContext;
import com.example.saasfile.common.Constants;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.common.utils.RandomUtil;
import com.example.saasfile.feign.entity.index.FileUploadResult;
import com.example.saasfile.feign.entity.index.R;
import com.example.saasfile.feign.entity.index.UploadResultItem;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import com.example.saasfile.service.IFileObjectService;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class IndexDataHandle implements BusinessDataHandler {

    private final IFileObjectService fileObjectService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${service.index.address:localhost}")
    private String indexUrl;

    private final RedisService redisService;

    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode();
    }

    @Override
    public void batchSave(List<Object> dataList) {
    }

    @Override
    public boolean isDirectHandler() {
        return true;
    }

    @Override
    public void processDirectly(File file, Long fileId, int rowCount) {
        invokeIndexSystemProcessing(file, fileId);
    }

    public void invokeIndexSystemProcessing(File file, Long fileId) {
        Long userId = fileObjectService.getUserIdByFileId(fileId);
        if (Objects.isNull(userId)) {
            return;
        }

        String url = indexUrl + "/indicator/report/excel/upload";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("tenant-id", TenantContext.getCurrentTenant());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", new FileSystemResource(file));
        body.add("user_id", userId);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<R> response = restTemplate.postForEntity(url, requestEntity, R.class);
            Optional<String> reportFileIdOpt = Optional.ofNullable(response.getBody())
                .map(r -> JSONUtil.toBean(JSONUtil.toJsonStr(r.getData()), FileUploadResult.class))
                .map(FileUploadResult::getItems)
                .orElse(Collections.emptyList())
                .stream()
                .filter(UploadResultItem::getUploadStatus)
                .map(UploadResultItem::getFileId)
                .findFirst();

            if (reportFileIdOpt.isPresent()) {
                ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(25, 30));
                String tenantId = TenantContext.getCurrentTenant();
                redisService.hSet(Constants.INDEX_PARSE_TASK, tenantId + "-" + fileId, reportFileIdOpt.get(), 10800);
            } else {
                log.error("Index service upload failed: {}", JSONUtil.toJsonStr(response));
                handleProcessingFailure(fileId, userId);
            }
        } catch (Exception e) {
            log.error("Index service request failed", e);
            handleProcessingFailure(fileId, userId);
        }
    }

    private void handleProcessingFailure(Long fileId, Long userId) {
        fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
        ProcessProgressSupport.notifyParseComplete(fileId, userId);
    }
}

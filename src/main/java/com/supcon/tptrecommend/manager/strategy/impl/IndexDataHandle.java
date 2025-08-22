package com.supcon.tptrecommend.manager.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.supcon.framework.redis.core.service.RedisService;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.Constants;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.common.utils.RandomUtil;
import com.supcon.tptrecommend.feign.entity.index.FileUploadResult;
import com.supcon.tptrecommend.feign.entity.index.R;
import com.supcon.tptrecommend.feign.entity.index.UploadResultItem;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 指标报表数据处理器
 *
 * @author luhao
 * @since 2025/07/02 10:49:01
 */
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
        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("tenant-id", TenantContext.getCurrentTenant());

        // 2. 创建 MultiValueMap 来构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 3. 将 File 包装成 FileSystemResource
        body.add("files", new FileSystemResource(file));
        body.add("user_id", userId);

        // 4. 将请求头和请求体封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 5. 发送 POST 请求
        try {
            ResponseEntity<R> response = restTemplate.postForEntity(url, requestEntity, R.class);
            Optional<String> reportFileIdOpt = Optional.ofNullable(response.getBody())
                .map(r -> JSONUtil.toBean(JSONUtil.toJsonStr(r.getData()), FileUploadResult.class))
                .map(FileUploadResult::getItems)
                .orElse(Collections.emptyList())
                .stream()
                .filter(uploadResultItem -> uploadResultItem.getUploadStatus() ||
                    uploadResultItem.getUploadMessage().contains("重复上传")) // 查找上传成功的条目
                .map(UploadResultItem::getFileId)         // 获取该条目的fileId
                .findFirst();
            // 4. 根据是否找到ID来处理成功或失败的情况
            if (reportFileIdOpt.isPresent()) {
                ProcessProgressSupport.notifyParseProcessing(fileId, userId,RandomUtil.getRandomPercentage(25, 30));
                String tenantId = TenantContext.getCurrentTenant();
                redisService.hSet(Constants.INDEX_PARSE_TASK, tenantId + "-" + fileId, reportFileIdOpt.get(),10800);
            } else {
                log.error("指标文件上传给指标服务失败:{}",JSONUtil.toJsonStr( response));
                handleProcessingFailure(fileId, userId);
            }
        } catch (Exception e) {
            log.error("请求指标系统处理解析报表数据失败", e);
            handleProcessingFailure(fileId, userId);
        }


    }

    private void handleProcessingFailure(Long fileId, Long userId) {
        fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
        ProcessProgressSupport.notifyParseComplete(fileId, userId);
    }


}

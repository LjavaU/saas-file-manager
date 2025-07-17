package com.supcon.tptrecommend.manager.strategy.impl;

import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.entity.FileObject;
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
import java.util.List;

/**
 * 指标报表数据处理器
 *
 * @author luhao
 * @since 2025/07/02 10:49:01
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsReportDataHandle implements BusinessDataHandler {

    private final IFileObjectService fileObjectService;


    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${service.index.address:localhost}")
    private String indexUrl;

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
        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("tenant-id", TenantContext.getCurrentTenant());

        // 2. 创建 MultiValueMap 来构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 3. 将 File 包装成 FileSystemResource
        body.add("files", new FileSystemResource(file));

        // 4. 将请求头和请求体封装到 HttpEntity 中
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 5. 发送 POST 请求
        try {
            ResponseEntity<SupResult> response = restTemplate.postForEntity(indexUrl, requestEntity, SupResult.class);
            if (response.getBody() == null || !response.getStatusCode().is2xxSuccessful() || !response.getBody().getSuccess()) {
                log.error("请求指标系统处理解析报表数据失败");
                fileObjectService.updateFileParseStatus(fileId, FileObject.FileStatus.PARSE_FAILED);
            } else {
                fileObjectService.updateFileParseStatus(fileId, FileObject.FileStatus.PARSED);
            }

        } catch (Exception e) {
            log.error("请求指标系统处理解析报表数据失败", e);
        }
        ProcessProgressSupport.notifyParseComplete(fileId);
    }
}

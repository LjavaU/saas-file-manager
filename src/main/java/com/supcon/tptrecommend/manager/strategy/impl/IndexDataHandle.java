package com.supcon.tptrecommend.manager.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.common.utils.RandomUtil;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.IndexFeign;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

    private final IndexFeign indexFeign;

    @Value("${service.index.address:localhost}")
    private String indexUrl;

    /**
     * 存储指标解析返回的文件id
     * key: 文件id
     * value: 报表文件id
     */
    private static final Map<Long, String> REPORT_FILE_ID = new ConcurrentHashMap<>();


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
        String url = indexUrl + "/indicator/report/excel/upload";
        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("tenant-id", TenantContext.getCurrentTenant());

        // 2. 创建 MultiValueMap 来构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 3. 将 File 包装成 FileSystemResource
        body.add("files", new FileSystemResource(file));
        // 由于上层调用使用了异步，所以在获取用户信息的时候不能再使用logUserUtils
        body.add("user_id", Optional.ofNullable(fileObjectService.getById(fileId)).map(FileObject::getUserId).orElse(1L));

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
                .filter(uploadResultItem -> {
                    return uploadResultItem.getUploadStatus() || uploadResultItem.getUploadMessage().contains("重复上传");
                }) // 查找上传成功的条目
                .map(UploadResultItem::getFileId)         // 获取该条目的fileId
                .findFirst();
            // 4. 根据是否找到ID来处理成功或失败的情况
            if (reportFileIdOpt.isPresent()) {
                ProcessProgressSupport.notifyParseProcessing(fileId, RandomUtil.getRandomPercentage(25, 30));
                REPORT_FILE_ID.put(fileId, reportFileIdOpt.get());
            }
        } catch (Exception e) {
            log.error("请求指标系统处理解析报表数据失败", e);
            handleProcessingFailure(fileId);
        }


    }

    private void handleProcessingFailure(Long fileId) {
        fileObjectService.updateFileParseStatus(fileId, FileObject.FileStatus.PARSE_FAILED);
        ProcessProgressSupport.notifyParseComplete(fileId);
    }


    /**
     * 每30S轮询一次
     *
     * @author luhao
     * @since 2025/07/24 15:30:50
     */
    @Scheduled(fixedDelay = 30000)
    public void getReportParseStatus() {
        if (REPORT_FILE_ID.isEmpty()) {
            return;
        }
        REPORT_FILE_ID.forEach((fileId, reportFileId) -> {
           R<String> reportParsingStatus = indexFeign.getReportParsingStatus(reportFileId, String.valueOf(Optional.ofNullable(fileObjectService.getById(fileId)).map(FileObject::getUserId).orElse(1L)));
            if (reportParsingStatus.isSuccess()) {
                String status = reportParsingStatus.getData();
                ReportParseStatus parseStatus = ReportParseStatus.getByValue(status);
                if (parseStatus != null) {
                    switch (parseStatus) {
                        case WAITING:
                            ProcessProgressSupport.notifyParseProcessing(fileId, 40);
                            break;
                        case PARSING:
                            ProcessProgressSupport.notifyParseProcessing(fileId, 55);
                            break;
                        case COMPLETED:
                        case PARTIAL_COMPLETION:
                            fileObjectService.updateFileParseStatus(fileId, FileObject.FileStatus.PARSED);
                            ProcessProgressSupport.notifyParseComplete(fileId);
                            REPORT_FILE_ID.remove(fileId);
                            break;
                        case ERROR:
                            fileObjectService.updateFileParseStatus(fileId, FileObject.FileStatus.PARSE_FAILED);
                            ProcessProgressSupport.notifyParseComplete(fileId);
                            REPORT_FILE_ID.remove(fileId);
                    }
                }
            } else {
                log.error("查询指标系统报表解析状态失败：{}", reportParsingStatus.getMsg());
            }
        });
    }

    /**
     * 报表解析状态
     *
     * @author luhao
     * @since 2025/07/24 14:40:50
     */
    enum ReportParseStatus {
        WAITING,
        PARSING,
        COMPLETED,
        ERROR,
        PARTIAL_COMPLETION;

        public static ReportParseStatus getByValue(String value) {
            for (ReportParseStatus status : values()) {
                if (status.name().equals(value)) {
                    return status;
                }
            }
            return null;
        }
    }
}

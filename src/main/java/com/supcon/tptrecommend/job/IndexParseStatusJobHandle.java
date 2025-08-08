package com.supcon.tptrecommend.job;

import com.supcon.framework.schedule.core.annotation.Job;
import com.supcon.framework.schedule.core.enums.ScheduleTypeEnum;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.IndexFeign;
import com.supcon.tptrecommend.feign.entity.index.R;
import com.supcon.tptrecommend.manager.strategy.impl.IndexDataHandle;
import com.supcon.tptrecommend.service.IFileObjectService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 指标文件解析状态任务
 *
 * @author luhao
 * @since 2025/08/05 14:16:36
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IndexParseStatusJobHandle {

    private final IFileObjectService fileObjectService;

    private final IndexFeign indexFeign;


    @XxlJob("indexParseStatusJob")
    @Job(jobDesc = "定时轮询指标文件解析状态", scheduleType = ScheduleTypeEnum.FIX_RATE, scheduleConf = "30", alarmEmail = "")
    public void execute() {
        Map<String, String> reportFileMap = IndexDataHandle.REPORT_FILE_ID;
        List<String> keysToRemove = new ArrayList<>();
        reportFileMap.forEach((tenantFileId, reportFileId) -> {
                String[] split = tenantFileId.split("-");
                String tenantId = split[0];
                Long fileId = Long.parseLong(split[1]);
                TenantContext.setCurrentTenant(tenantId);
                R<String> reportParsingStatus = indexFeign.getReportParsingStatus(reportFileId, String.valueOf(Optional.ofNullable(fileObjectService.getById(fileId)).map(FileObject::getUserId).orElse(1L)));
                if (Objects.nonNull(reportParsingStatus) && reportParsingStatus.isSuccess()) {
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
                                fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
                                keysToRemove.add(tenantFileId);
                                ProcessProgressSupport.notifyParseComplete(fileId);
                                break;
                            case ERROR:
                                fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
                                keysToRemove.add(tenantFileId);
                                ProcessProgressSupport.notifyParseComplete(fileId);
                        }
                    }

                } else {
                    log.error("查询指标系统报表解析状态失败：{}", Objects.nonNull(reportParsingStatus) ? reportParsingStatus.getMsg() : "请求失败");
                }
                TenantContext.clear();
            }
        );
        keysToRemove.forEach(reportFileMap::remove);
    }

    /**
     * 报表解析状态
     *
     * @author luhao
     * @since 2025/07/24 14:40:50
     */
    enum ReportParseStatus {
        WAITING, PARSING, COMPLETED, ERROR, PARTIAL_COMPLETION;

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

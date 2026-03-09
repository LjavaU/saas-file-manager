package com.example.saasfile.job;

import com.example.saasfile.support.redis.RedisService;
import com.example.saasfile.support.schedule.Job;
import com.example.saasfile.support.schedule.ScheduleTypeEnum;
import com.example.saasfile.support.tenant.TenantContext;
import com.example.saasfile.common.Constants;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.feign.IndexFeign;
import com.example.saasfile.feign.entity.index.R;
import com.example.saasfile.service.IFileObjectService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class IndexParseStatusJobHandle {

    private final IFileObjectService fileObjectService;
    private final IndexFeign indexFeign;
    private final RedisService redisService;

    @XxlJob("indexParseStatusJob")
    @Job(jobDesc = "Poll index parse status", scheduleType = ScheduleTypeEnum.FIX_RATE, scheduleConf = "30", alarmEmail = "")
    public void execute() {
        Map<String, String> fileTaskMap = redisService.hGetAll(Constants.INDEX_PARSE_TASK);
        if (fileTaskMap == null || fileTaskMap.isEmpty()) {
            return;
        }

        List<String> keysToRemove = new ArrayList<>();
        fileTaskMap.forEach((tenantFileId, reportFileId) -> {
            String[] split = tenantFileId.split("-");
            String tenantId = split[0];
            Long fileId = Long.parseLong(split[1]);
            TenantContext.setCurrentTenant(tenantId);
            try {
                Long userId = fileObjectService.getUserIdByFileId(fileId);
                if (Objects.isNull(userId)) {
                    keysToRemove.add(tenantFileId);
                    return;
                }

                R<String> reportParsingStatus = indexFeign.getReportParsingStatus(reportFileId, String.valueOf(userId));
                if (Objects.nonNull(reportParsingStatus) && reportParsingStatus.isSuccess()) {
                    String status = reportParsingStatus.getData();
                    ReportParseStatus parseStatus = ReportParseStatus.getByValue(status);
                    if (parseStatus != null) {
                        switch (parseStatus) {
                            case WAITING:
                                ProcessProgressSupport.notifyParseProcessing(fileId, userId, 40);
                                break;
                            case PARSING:
                                ProcessProgressSupport.notifyParseProcessing(fileId, userId, 55);
                                break;
                            case COMPLETED:
                            case PARTIAL_COMPLETION:
                                fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
                                keysToRemove.add(tenantFileId);
                                ProcessProgressSupport.notifyParseComplete(fileId, userId);
                                break;
                            case ERROR:
                                fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSE_FAILED);
                                keysToRemove.add(tenantFileId);
                                ProcessProgressSupport.notifyParseComplete(fileId, userId);
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    log.error("Failed to query index parse status: {}", Objects.nonNull(reportParsingStatus) ? reportParsingStatus.getMsg() : "request failed");
                }
            } finally {
                TenantContext.clear();
            }
        });

        if (!keysToRemove.isEmpty()) {
            redisService.hDel(Constants.INDEX_PARSE_TASK, keysToRemove.toArray());
        }
    }

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

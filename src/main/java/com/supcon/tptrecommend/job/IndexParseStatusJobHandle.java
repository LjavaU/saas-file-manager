package com.supcon.tptrecommend.job;

import com.supcon.framework.redis.core.service.RedisService;
import com.supcon.framework.schedule.core.annotation.Job;
import com.supcon.framework.schedule.core.enums.ScheduleTypeEnum;
import com.supcon.framework.tenant.core.getter.TenantContext;
import com.supcon.tptrecommend.common.Constants;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.feign.IndexFeign;
import com.supcon.tptrecommend.feign.entity.index.R;
import com.supcon.tptrecommend.service.IFileObjectService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final RedisService redisService;


    @XxlJob("indexParseStatusJob")
    @Job(jobDesc = "定时轮询指标文件解析状态", scheduleType = ScheduleTypeEnum.FIX_RATE, scheduleConf = "30", alarmEmail = "")
    public void execute() {
        Map<String, String> fileTaskMap = redisService.hGetAll(Constants.INDEX_PARSE_TASK);
        List<String> keysToRemove = new ArrayList<>();
        fileTaskMap.forEach((tenantFileId, reportFileId) -> {
                String[] split = tenantFileId.split("-");
                String tenantId = split[0];
                Long fileId = Long.parseLong(split[1]);
                TenantContext.setCurrentTenant(tenantId);
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
                        }
                    }

                } else {
                    log.error("查询指标系统报表解析状态失败：{}", Objects.nonNull(reportParsingStatus) ? reportParsingStatus.getMsg() : "请求失败");
                }
                TenantContext.clear();
            }
        );
        // 移除已经处理完成的报表
        redisService.hDel(Constants.INDEX_PARSE_TASK,keysToRemove.toArray());
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

package com.supcon.tptrecommend.job;

import com.supcon.framework.schedule.core.enums.ScheduleTypeEnum;
import com.supcon.framework.schedule.core.handler.IJob;
import lombok.Data;

import java.util.Map;

/**
 * 知识库解析状态任务
 *
 * @author luhao
 * @since 2025/07/30 09:44:27
 */
@Data
public class KnowledgeParseStatusJob implements IJob {
    private Integer xxlJobId;
    private String jobHandler = "knowledgeParseStatusJob";
    private Map<String, Object> userJobParam;

    @Override
    public Integer xxlJobId() {
        return xxlJobId;
    }

    @Override
    public String jobHandler() {
        return jobHandler;
    }

    @Override
    public String jobDesc() {
        return "定时轮询文件解析的状态";
    }

    @Override
    public String alarmEmail() {
        return null;
    }

    @Override
    public ScheduleTypeEnum scheduleType() {
        return ScheduleTypeEnum.FIX_RATE;
    }

    @Override
    public String scheduleConf() {
        return "30";
    }

}
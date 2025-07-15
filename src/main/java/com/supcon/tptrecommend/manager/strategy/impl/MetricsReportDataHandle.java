package com.supcon.tptrecommend.manager.strategy.impl;

import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
public class MetricsReportDataHandle implements BusinessDataHandler {
    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode();
    }

    @Override
    public Class<?> getEntityClass() {
        return null;
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
        // 把file转换为mutipartFile
        log.info("开始处理指标业务报表数据");

    }
}

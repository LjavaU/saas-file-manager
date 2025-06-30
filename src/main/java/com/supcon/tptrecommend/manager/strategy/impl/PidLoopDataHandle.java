package com.supcon.tptrecommend.manager.strategy.impl;

import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.feign.PidFeign;
import com.supcon.tptrecommend.feign.entity.pid.DcsLoopMetadata;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PidLoopDataHandle implements BusinessDataHandler {

    private final PidFeign pidFeign;

    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.DCS_POINT_TABLE_FB.getCode();
    }

    @Override
    public Class<?> getEntityClass() {
        return DcsLoopMetadata.class;
    }


    @Override
    public void batchSave(List<Object> dataList) {
        List<DcsLoopMetadata> dcsLoopMetadataList = castTargetObject(dataList, DcsLoopMetadata.class);
        dcsLoopMetadataList = dcsLoopMetadataList.stream()
            .filter(dcsLoopMetadatum -> "PID".equalsIgnoreCase(dcsLoopMetadatum.getFunctionBlockType())
                || "PIDEX".equalsIgnoreCase(dcsLoopMetadatum.getFunctionBlockType())).collect(Collectors.toList());
        for (DcsLoopMetadata dcsLoopMetadatum : dcsLoopMetadataList) {
            SupResult<Object> addLoop = pidFeign.addLoop(dcsLoopMetadatum);
            if (addLoop.getSuccess()) {
                log.info("PID回路数据保存成功");
            } else {
                log.error("PID回路数据保存失败:{}", addLoop.getMsg());
            }
        }


    }
}

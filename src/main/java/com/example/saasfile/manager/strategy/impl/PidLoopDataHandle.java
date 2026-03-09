package com.example.saasfile.manager.strategy.impl;

import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.feign.PidFeign;
import com.example.saasfile.feign.entity.pid.DcsLoopMetadata;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
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
            if (!addLoop.isSuccess()) {
                log.error("PID閸ョ偠鐭鹃弫鐗堝祦娣囨繂鐡ㄦ径杈Е:{}", addLoop.getMsg());
            }
        }


    }
}

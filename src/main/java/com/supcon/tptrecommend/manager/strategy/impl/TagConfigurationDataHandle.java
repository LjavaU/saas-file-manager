package com.supcon.tptrecommend.manager.strategy.impl;

import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.feign.DataHubFeign;
import com.supcon.tptrecommend.feign.entity.datahub.TagInfoCreateReq;
import com.supcon.tptrecommend.feign.entity.datahub.TagInfoResp;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 位号组态数据处理
 *
 * @author luhao
 * @since 2025/07/15 13:33:19
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TagConfigurationDataHandle implements BusinessDataHandler {

    private final DataHubFeign dataHubFeign;

    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.TAG_CONFIGURATION.getCode();
    }

    @Override
    public Class<?> getEntityClass() {
        return TagInfoCreateReq.class;
    }

    @Override
    public void batchSave(List<Object> dataList) {
        List<TagInfoCreateReq> tagInfoCreateReqs = castTargetObject(dataList, TagInfoCreateReq.class);
        SupResult<List<TagInfoResp>> result = dataHubFeign.batchAdd(SupRequestBody.data(tagInfoCreateReqs));
        if (!result.getSuccess()) {
            log.error("位号组态数据保存失败：{}",result.getMsg());
        }

    }
}

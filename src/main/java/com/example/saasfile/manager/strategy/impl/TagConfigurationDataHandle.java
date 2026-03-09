package com.example.saasfile.manager.strategy.impl;

import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.feign.DataHubFeign;
import com.example.saasfile.feign.entity.datahub.TagInfoCreateReq;
import com.example.saasfile.feign.entity.datahub.TagInfoResp;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 娴ｅ秴褰跨紒鍕偓浣规殶閹诡喖顦╅悶?
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
        if (!result.isSuccess()) {
            log.error("娴ｅ秴褰跨紒鍕偓浣规殶閹诡喕绻氱€涙ê銇戠拹銉窗{}",result.getMsg());
        }

    }
}

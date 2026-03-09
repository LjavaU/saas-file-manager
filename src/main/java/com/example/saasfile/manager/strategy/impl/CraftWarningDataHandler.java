package com.example.saasfile.manager.strategy.impl;

import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.feign.AutoSupervisionFeign;
import com.example.saasfile.feign.entity.autosupervision.ControlExcelExport;
import com.example.saasfile.feign.entity.autosupervision.TagExcelImport;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CraftWarningDataHandler implements BusinessDataHandler {

    private final AutoSupervisionFeign autoSupervisionFeign;

    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.EARLY_WARNING_PROCESS_ABNORMALITIES.getCode();
    }

    @Override
    public Class<?> getEntityClass() {
        return TagExcelImport.class;
    }

    @Override
    public void batchSave(List<Object> dataList) {
        List<TagExcelImport> tagExcelImports = castTargetObject(dataList, TagExcelImport.class);
        SupResult<List<ControlExcelExport>> supResult = autoSupervisionFeign.importBottomTagExcelOrigin(SupRequestBody.data(tagExcelImports), 6);
        if (supResult.isSuccess()) {
            log.info("Craft warning data saved successfully");
        } else {
            log.error("Craft warning data save failed: {}", supResult.getMsg());
        }
    }
}

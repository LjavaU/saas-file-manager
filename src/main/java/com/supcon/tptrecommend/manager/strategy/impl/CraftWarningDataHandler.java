package com.supcon.tptrecommend.manager.strategy.impl;

import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.feign.AutoSupervisionFeign;
import com.supcon.tptrecommend.feign.entity.autosupervision.ControlExcelExport;
import com.supcon.tptrecommend.feign.entity.autosupervision.TagExcelImport;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工艺预警数据处理器
 *
 * @author luhao
 * @since 2025/06/25 19:03:49
 */
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
        if (supResult.getSuccess()) {
            log.info("工艺预警数据保存成功");
        } else {
            log.error("工艺预警数据保存失败:{}", supResult.getMsg());
        }

    }
}
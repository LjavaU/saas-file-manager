package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 业务分类枚举
 *
 * @author luhao
 * @since 2025/06/23 15:23:12
 */
@Getter
@AllArgsConstructor
public enum SubCategoryEnum {
    IDENTIFICATION_MODEL_INFO(0, "辨识模型信息"),
    DATA_SOURCE_CONFIG_INFO(1, "数据源配置信息"),
    MATERIAL_PARAMETER_PROPERTY(2, "物料参数属性"),
    MATERIAL_PARAMETER_EQUATION(3, "物料参数方程"),
    MATERIAL_BALANCE_SHEET(4, "物料平衡表"),
    EQUIPMENT_INFO(5, "设备信息"),
    WORKING_CONDITION_INFO(6, "工况信息"),
    TAG_CONFIGURATION(7, "位号组态"),
    LOOP_CONFIGURATION(8, "回路组态"),
    ALARM_TAG_MAPPING_TABLE(9, "报警位号映射表"),
    DYNAMIC_EQUIPMENT_INFO_MODEL(10, "动设备信息模型"),
    TAG_HISTORICAL_DATA(11, "位号历史数据"),
    HEAT_EXCHANGER_NETWORK_DESIGN_INFO(12, "换热网络设计信息"),
    OPERATING_PROCEDURE_DEVICE(13, "操作规程装置"),
    PROCESS_INDEX_COMPOSITION(14, "工艺指标组成"),
    DCS_POINT_TABLE_FB(15, "DCS点表FB"),
    EARLY_WARNING_PROCESS_ABNORMALITIES(16, "工艺异常预警"),
    METRICS_BUSINESS_REPORT_DATA(17, "指标业务报表数据"),
    EXAM_QUESTION_DATA(18, "考题资料数据"),
    OVERALL_INFORMATION_DEVICE(19, "装置整体信息"),
    CONDITION_INPUT(20, "限制条件输入"),
    ATMOSPHERIC_PRESSURE_TOWERS(21, "近期运行数据"),
    KEY_PARAMETERS(22, "关键参数");

    private final Integer code;
    private final String description;

    public static String fromCode(int code) {
        for (SubCategoryEnum e : values()) {
            if (e.code == code) {
                return e.getDescription();
            }
        }
        return null;

    }
    // 根据code获取枚举
    public static SubCategoryEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(SubCategoryEnum.values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据codes 获取 描述
     *
     * @param codes 代码
     * @return {@link String }
     * @author luhao
     * @since 2025/08/21 17:06:28
     *
     */
    public static String getDescriptionByCodes(List<Integer> codes) {
        return codes.stream()
            .map(SubCategoryEnum::getByCode)
            .filter(Objects::nonNull)
            .map(SubCategoryEnum::getDescription)
            .collect(Collectors.joining(","));
    }

}

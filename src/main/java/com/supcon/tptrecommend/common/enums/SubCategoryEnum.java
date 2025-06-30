package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    EQUIPMENT_LIST(5, "设备一览表"),
    EQUIPMENT_STRUCTURE_INFO(6, "设备结构信息"),
    WORKING_CONDITION_INFO(7, "工况信息"),
    TAG_CONFIGURATION(8, "位号组态"),
    LOOP_CONFIGURATION(9, "回路组态"),
    ALARM_TAG_MAPPING_TABLE(10, "报警位号映射表"),
    DYNAMIC_EQUIPMENT_INFO_MODEL(11, "动设备信息模型"),
    TAG_HISTORICAL_DATA(12, "位号历史数据"),
    PUMP_ANOMALY_DETECTION_DATA(13, "泵异常检测数据"),
    VALVE_ANOMALY_DETECTION_DATA(14, "阀门异常检测数据"),
    HEAT_EXCHANGER_NETWORK_DESIGN_INFO(15, "换热网络设计信息"),
    HEAT_EXCHANGER_NETWORK_OPERATION_DATA(16, "换热网络运行数据"),
    OPERATING_PROCEDURE_DEVICE(17, "操作规程装置"),
    PROCESS_INDEX_COMPOSITION(18, "工艺指标组成"),
    DCS_POINT_TABLE_FB(19, "DCS点表FB"),
    EARLY_WARNING_PROCESS_ABNORMALITIES(20, "工艺异常预警");

    private final int code;
    private final String description;

    public static String fromCode(int code) {
        for (SubCategoryEnum e : values()) {
            if (e.code == code) {
                return e.getDescription();
            }
        }
        return null;

    }
}

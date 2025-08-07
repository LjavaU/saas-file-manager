package com.supcon.tptrecommend.common.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.supcon.tptrecommend.common.enums.SubCategoryEnum.*;
import static com.supcon.tptrecommend.common.enums.TagHistoryCategory.*;

/**
 * 文件类别能力关联
 *
 * @author luhao
 * @since 2025/08/06 10:49:32
 */
@Getter
@AllArgsConstructor
public enum FileCategoryAbilityAssociation {
    // --- 类别（无详细能力） ---
    IDENTIFICATION_MODEL_INFO_DEFAULT(IDENTIFICATION_MODEL_INFO, null, null, "辨识模型信息", null, null),
    DATA_SOURCE_CONFIG_INFO_DEFAULT(DATA_SOURCE_CONFIG_INFO, null, null, "数据源配置信息", null, null),
    MATERIAL_PARAMETER_PROPERTY_DEFAULT(MATERIAL_PARAMETER_PROPERTY, null, null, "物料参数属性", null, null),
    MATERIAL_PARAMETER_EQUATION_DEFAULT(MATERIAL_PARAMETER_EQUATION, null, null, "物料参数方程", null, null),
    MATERIAL_BALANCE_SHEET_DEFAULT(MATERIAL_BALANCE_SHEET, null, null, "物料平衡表", null, null),
    EQUIPMENT_LIST_DEFAULT(EQUIPMENT_LIST, null, null, "设备一览表", null, null),
    EQUIPMENT_STRUCTURE_INFO_DEFAULT(EQUIPMENT_STRUCTURE_INFO, null, null, "设备结构信息", null, null),
    WORKING_CONDITION_INFO_DEFAULT(WORKING_CONDITION_INFO, null, null, "工况信息", null, null),
    TAG_CONFIGURATION_DEFAULT(TAG_CONFIGURATION, null, null, "位号组态", null, null),
    ALARM_TAG_MAPPING_TABLE_DEFAULT(ALARM_TAG_MAPPING_TABLE, null, null, "报警位号映射表", null, null),
    DYNAMIC_EQUIPMENT_INFO_MODEL_DEFAULT(DYNAMIC_EQUIPMENT_INFO_MODEL, null, null, "动设备信息模型", null, null),
    PUMP_ANOMALY_DETECTION_DATA_DEFAULT(PUMP_ANOMALY_DETECTION_DATA, null, null, "泵异常检测数据", null, null),
    VALVE_ANOMALY_DETECTION_DATA_DEFAULT(VALVE_ANOMALY_DETECTION_DATA, null, null, "阀门异常检测数据", null, null),
    OPERATING_PROCEDURE_DEVICE_DEFAULT(OPERATING_PROCEDURE_DEVICE, null, null, "操作规程装置", null, null),
    PROCESS_INDEX_COMPOSITION_DEFAULT(PROCESS_INDEX_COMPOSITION, null, null, "工艺指标组成", null, null),
    DCS_POINT_TABLE_FB_DEFAULT(DCS_POINT_TABLE_FB, null, null, "DCS点表FB", null, null),
    EARLY_WARNING_PROCESS_ABNORMALITIES_DEFAULT(EARLY_WARNING_PROCESS_ABNORMALITIES, null, null, "工艺异常预警", null, null),

    // --- 具有详细能力的类别 ---
    F_LOOP_CONFIG_BATCH_PRE_TUNING(LOOP_CONFIGURATION, null, "f_loop_config", "回路组态", "batch_pre_tuning", "开车前回路参数批量整定"),

    F_LOOP_HISTORY_IMC_TUNING(TAG_HISTORICAL_DATA, CIRCUIT_HISTORY_DATA, "f_loop_history", "回路历史数据", "imc_tuning", "基于历史数据的参数整定/基于历史数据的回路整定/IMC整定"),
    F_LOOP_HISTORY_GUIDE_TUNING(TAG_HISTORICAL_DATA, CIRCUIT_HISTORY_DATA, "f_loop_history", "回路历史数据", "guide_tuning", "智能向导整定/方案确认自动寻优"),
    F_LOOP_HISTORY_DATA_VALIDATION(TAG_HISTORICAL_DATA, CIRCUIT_HISTORY_DATA, "f_loop_history", "回路历史数据", "data_validation", "回路数据初筛/数据校验"),

    F_TAG_HISTORY_DATA_GET_AUTO_IDENTIFICATION_ADD(TAG_HISTORICAL_DATA, TAG_HISTORY_DATA, "f_tag_history_data", "位号历史数据", "get_auto_identification_add", "上传数据后校验"),
    F_VARIABLE_HISTORY_DATA_GET_CONTROL_SCHEME_GENERATION(TAG_HISTORICAL_DATA, VARIABLE_HISTORY_DATA, "f_variable_history_data", "变量历史数据", "get_control_scheme_generation", "数据处理与质量判断/转换质量分析"),
    OPT_HISTORY_DATA_OPT_OFFLINE_FILE_UPLOAD(TAG_HISTORICAL_DATA, OPTIMIZATION_HISTORY_DATA, "opt_history_data", "优化历史数据", "opt_offline_file_upload", "PID数据处理与质量判断/PID数据质量分析"),
    ALARM_PREDICTION_HISTORY_DATA_AAL_OFFLINE_FILE_UPLOAD(TAG_HISTORICAL_DATA, ALARM_PREDICTION_HISTORY_DATA, "alarm_prediction_history_data", "预警预测历史数据", "aal_offline_file_upload", "建模数据上传"),
    HEN_REDESIGN_HISTORY_INFO_CHECK(TAG_HISTORICAL_DATA, HEAT_EXCHANGER_NETWORK_OPERATION_INFO, "hen_redesign_history", "换热网络运行信息", "info_check", "上传数据后校验"),
    F_SPC_DATA_SPC_CSV_UPLOAD(TAG_HISTORICAL_DATA, TAG_DATA_FILE, "f_spc_data", "位号数据文件", "spc_csv_upload", "数据处理与质量判断/转换质量分析"),
    F_SPC_PID_DATA_SPC_PID_CSV_UPLOAD(TAG_HISTORICAL_DATA, CIRCUIT_DATA_FILE, "f_spc_pid_data", "回路数据文件", "spc_PID_csv_upload", "PID数据处理与质量判断/PID数据质量分析"),

    HEN_REDESIGN_DESIGN_INFO_INFO_CHECK(HEAT_EXCHANGER_NETWORK_DESIGN_INFO, null, "hen_redesign_design_info", "换热网络设计信息", "info_check", "上传数据与校验"),
    F_IDX_REPORT_DATA_REPORT(METRICS_BUSINESS_REPORT_DATA, null, "f_idx_report", "指标业务报表数据", "data_report", "报表数据解析"),
    EXAM_DATA_PROCESS_RECEIVED_FORM(EXAM_QUESTION_DATA, null, "exam_data", "考题资料数据", "process_received_form", "考题自动生成"),
    REDESIGN_ENERGY_INFO_EXERGY_CAL(OVERALL_INFORMATION_DEVICE, null, "redesign_energy_info", "装置整体信息", "exergy_cal", "节能潜力消耗分析"),
    HEN_REDESIGN_RESTRICTED_SELECT_FORBIDDEN_RULE(CONDITION_INPUT, null, "hen_redesign_restricted", "限制条件输入", "select_forbidden_rule", "给定厂级限制条件");

    /**
     * 类别 (来源于 CategoryTypeEnum)
     */
    private final SubCategoryEnum categoryType;

    /**
     * 位号历史类型
     */
    private final TagHistoryCategory tagHistoryCategory;

    /**
     * 类别标识 (e.g., f_loop_config)
     */
    private final String categoryIdentifier;

    /**
     * 类别名 (e.g., 回路组态)
     */
    private final String categoryName;

    /**
     * 关联能力
     */
    private final String associatedCapability;

    /**
     * 能力描述
     */
    private final String capabilityDescription;


    /**
     * 根据类别标识获取categoryType
     *
     * @param identifier 标识符
     * @return {@link FileCategoryAbilityAssociation }
     * @author luhao
     * @since 2025/08/06 10:59:47
     *
     */
    public static FileCategoryAbilityAssociation getCategoryByIdentifier(String identifier) {
        for (FileCategoryAbilityAssociation value : FileCategoryAbilityAssociation.values()) {
            if (value.getCategoryIdentifier() != null && value.categoryIdentifier.equals(identifier)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据类别code获取类别名
     *
     * @param codes 二级分类编码
     * @return {@link String }
     * @author luhao
     * @since 2025/08/06 11:01:26
     *
     *
     */
    public static String getCategoryNameBySubCategoryCode(List<Integer> codes) {
        List<SubCategoryEnum> subCategoryEnums = Arrays.stream(SubCategoryEnum.values())
            .filter(item ->codes.contains(item.getCode()))
            .collect(Collectors.toList());
        return Arrays.stream(FileCategoryAbilityAssociation.values())
            .filter(item -> subCategoryEnums.contains(item.getCategoryType()))
            .map(FileCategoryAbilityAssociation::getCategoryName)
            .collect(Collectors.joining(","));
    }

    /**
     * 根据类别code获取类别标识
     *
     * @param codes 二级分类编码
     * @return {@link String }
     * @author luhao
     * @since 2025/08/06 15:22:02
     *
     *
     */
    public static String getCategoryIdentifierBySubCategoryCode(List<Integer> codes) {
        List<SubCategoryEnum> subCategoryEnums = Arrays.stream(SubCategoryEnum.values())
            .filter(item -> codes.contains(item.getCode()))
            .collect(Collectors.toList());
        return Arrays.stream(FileCategoryAbilityAssociation.values())
            .filter(item -> subCategoryEnums.contains(item.getCategoryType()))
            .map(FileCategoryAbilityAssociation::getCategoryIdentifier)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }

    /**
     * 根据三级类别code获取类别标识
     *
     * @param codes 三级分类编码
     * @return {@link String }
     * @author luhao
     * @since 2025/08/06 15:32:06
     *
     *
     */
    public static String getCategoryIdentifierByTagHistoryCode(List<Integer> codes) {
        List<TagHistoryCategory> tagHistoryCategoryEnums = Arrays.stream(TagHistoryCategory.values())
            .filter(item -> codes.contains(item.getCode()))
            .collect(Collectors.toList());
        return Arrays.stream(FileCategoryAbilityAssociation.values())
            .filter(item -> tagHistoryCategoryEnums.contains(item.getTagHistoryCategory()))
            .map(FileCategoryAbilityAssociation::getCategoryIdentifier)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }


    /**
     * 根据三级类别获取能力
     *
     * @param tagHistoryCategory 位号历史类别
     * @return {@link String }
     * @author luhao
     * @since 2025/08/06 16:03:38
     *
     */
    public static String getAbilityByTagHistoryCategory(TagHistoryCategory tagHistoryCategory) {
        if (tagHistoryCategory == null) {
            return null;
        }
        return Arrays.stream(FileCategoryAbilityAssociation.values())
            .filter(item -> tagHistoryCategory.equals(item.getTagHistoryCategory()))
            .map(FileCategoryAbilityAssociation::getAssociatedCapability)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));

    }

    /**
     * 根据二级类别获取能力
     *
     * @param subCategoryEnum 子类别枚举
     * @return {@link String }
     * @author luhao
     * @since 2025/08/06 16:03:24
     *
     */
    public static String getAbilityBySubCategory(SubCategoryEnum subCategoryEnum) {
        if (subCategoryEnum == null) {
            return null;
        }
        return Arrays.stream(FileCategoryAbilityAssociation.values())
            .filter(item -> subCategoryEnum.equals(item.getCategoryType()))
            .map(FileCategoryAbilityAssociation::getAssociatedCapability)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));

    }

    /**
     * 根据能力获取能力描述
     *
     * @param abilities 能力
     * @return {@link String }
     * @author luhao
     * @since 2025/08/07 10:33:37
     *
     */
    public static String getAbilityDescriptionByAbility(List<String> abilities) {
        return Arrays.stream(FileCategoryAbilityAssociation.values())
            .filter(item -> abilities.contains(item.getAssociatedCapability()))
            .map(FileCategoryAbilityAssociation::getCapabilityDescription)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));

    }
}

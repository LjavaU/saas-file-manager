package com.example.saasfile.common.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.saasfile.common.enums.SubCategoryEnum.*;
import static com.example.saasfile.common.enums.TagHistoryCategory.*;

@Getter
@AllArgsConstructor
public enum FileCategoryAbilityAssociation {
    IDENTIFICATION_MODEL_INFO_DEFAULT(IDENTIFICATION_MODEL_INFO, null, null, "Identification Model Info", null, null),
    DATA_SOURCE_CONFIG_INFO_DEFAULT(DATA_SOURCE_CONFIG_INFO, null, null, "Data Source Config Info", null, null),
    MATERIAL_PARAMETER_PROPERTY_DEFAULT(MATERIAL_PARAMETER_PROPERTY, null, null, "Material Parameter Property", null, null),
    MATERIAL_PARAMETER_EQUATION_DEFAULT(MATERIAL_PARAMETER_EQUATION, null, null, "Material Parameter Equation", null, null),
    MATERIAL_BALANCE_SHEET_DEFAULT(MATERIAL_BALANCE_SHEET, null, null, "Material Balance Sheet", null, null),
    EQUIPMENT_LIST_INFO(EQUIPMENT_INFO, null, null, "Equipment Info", null, null),
    WORKING_CONDITION_INFO_DEFAULT(WORKING_CONDITION_INFO, null, null, "Working Condition Info", null, null),
    TAG_CONFIGURATION_DEFAULT(TAG_CONFIGURATION, null, null, "Tag Configuration", null, null),
    ALARM_TAG_MAPPING_TABLE_DEFAULT(ALARM_TAG_MAPPING_TABLE, null, null, "Alarm Tag Mapping Table", null, null),
    DYNAMIC_EQUIPMENT_INFO_MODEL_DEFAULT(DYNAMIC_EQUIPMENT_INFO_MODEL, null, null, "Dynamic Equipment Info Model", null, null),
    OPERATING_PROCEDURE_DEVICE_DEFAULT(OPERATING_PROCEDURE_DEVICE, null, null, "Operating Procedure Device", null, null),
    PROCESS_INDEX_COMPOSITION_DEFAULT(PROCESS_INDEX_COMPOSITION, null, null, "Process Index Composition", null, null),
    DCS_POINT_TABLE_FB_DEFAULT(DCS_POINT_TABLE_FB, null, null, "DCS Point Table FB", null, null),
    EARLY_WARNING_PROCESS_ABNORMALITIES_DEFAULT(EARLY_WARNING_PROCESS_ABNORMALITIES, null, null, "Early Warning Process Abnormalities", null, null),

    F_LOOP_CONFIG_BATCH_PRE_TUNING(LOOP_CONFIGURATION, null, "f_loop_config", "Loop Configuration", "batch_pre_tuning", "Batch pre-tuning"),
    F_LOOP_HISTORY_IMC_TUNING(TAG_HISTORICAL_DATA, CIRCUIT_HISTORY_DATA, "f_loop_history", "Loop History Data", "imc_tuning", "IMC tuning"),
    F_LOOP_HISTORY_GUIDE_TUNING(TAG_HISTORICAL_DATA, CIRCUIT_HISTORY_DATA, "f_loop_history", "Loop History Data", "guide_tuning", "Guide tuning"),
    F_LOOP_HISTORY_DATA_VALIDATION(TAG_HISTORICAL_DATA, CIRCUIT_HISTORY_DATA, "f_loop_history", "Loop History Data", "data_validation", "Data validation"),
    F_TAG_HISTORY_DATA_GET_AUTO_IDENTIFICATION_ADD(TAG_HISTORICAL_DATA, TAG_HISTORY_DATA, "f_tag_history_data", "Tag History Data", "get_auto_identification_add", "Auto identification"),
    F_VARIABLE_HISTORY_DATA_GET_CONTROL_SCHEME_GENERATION(TAG_HISTORICAL_DATA, VARIABLE_HISTORY_DATA, "f_variable_history_data", "Variable History Data", "get_control_scheme_generation", "Control scheme generation"),
    OPT_HISTORY_DATA_OPT_OFFLINE_FILE_UPLOAD(TAG_HISTORICAL_DATA, OPTIMIZATION_HISTORY_DATA, "opt_history_data", "Optimization History Data", "opt_offline_file_upload", "Offline optimization upload"),
    ALARM_PREDICTION_HISTORY_DATA_AAL_OFFLINE_FILE_UPLOAD(TAG_HISTORICAL_DATA, ALARM_PREDICTION_HISTORY_DATA, "alarm_prediction_history_data", "Alarm Prediction History Data", "aml_offline_file_upload", "Alarm model upload"),
    HEN_REDESIGN_HISTORY_INFO_CHECK(TAG_HISTORICAL_DATA, HEAT_EXCHANGER_NETWORK_OPERATION_INFO, "hen_redesign_history", "Heat Exchanger Operation Info", "info_check", "Operation info check"),
    F_SPC_DATA_SPC_CSV_UPLOAD(TAG_HISTORICAL_DATA, TAG_DATA_FILE, "f_spc_data", "Tag Data File", "spc_csv_upload", "SPC CSV upload"),
    F_SPC_PID_DATA_SPC_PID_CSV_UPLOAD(TAG_HISTORICAL_DATA, CIRCUIT_DATA_FILE, "f_spc_pid_data", "Circuit Data File", "spc_PID_csv_upload", "SPC PID CSV upload"),
    ANOMALY_DETECTION_HISTORICAL_DATA_OFFLINE(TAG_HISTORICAL_DATA, ANOMALY_DETECTION_HISTORICAL_DATA, "anomaly_detection_history_data", "Anomaly Detection History Data", "ad_offline_file_upload", "Offline anomaly upload"),
    ANOMALY_DETECTION_HISTORICAL_FREQUENCY_DATA_FREQUENCY(TAG_HISTORICAL_DATA, ANOMALY_DETECTION_HISTORICAL_DATA, "anomaly_detection_history_data", "Anomaly Detection History Data", "high_frequency_upload_file", "High frequency upload"),
    BOILER_HISTORICAL_DATA_OFFLINE(TAG_HISTORICAL_DATA, BOILER_HISTORICAL_DATA, "f_boiler_history_data", "Boiler History Data", "upload_boiler_offline_file", "Boiler offline upload"),

    HEN_REDESIGN_DESIGN_INFO_INFO_CHECK(HEAT_EXCHANGER_NETWORK_DESIGN_INFO, null, "hen_redesign_design_info", "Heat Exchanger Design Info", "info_check", "Design info check"),
    F_IDX_REPORT_DATA_REPORT(METRICS_BUSINESS_REPORT_DATA, null, "f_idx_report", "Metrics Business Report Data", "data_report", "Metrics data report"),
    EXAM_DATA_PROCESS_RECEIVED_FORM(EXAM_QUESTION_DATA, null, "exam_data", "Exam Question Data", "process_received_form", "Question import"),
    REDESIGN_ENERGY_INFO_EXERGY_CAL(OVERALL_INFORMATION_DEVICE, null, "redesign_energy_info", "Overall Information Device", "exergy_cal", "Exergy calculation"),
    HEN_REDESIGN_RESTRICTED_SELECT_FORBIDDEN_RULE(CONDITION_INPUT, null, "hen_redesign_restricted", "Condition Input", "select_forbidden_rule", "Forbidden rule selection"),
    KEY_PARAMETERS_INFO(KEY_PARAMETERS, null, "key_variables_info", "Key Parameters", "get_call_keyword", "Keyword extraction"),
    RECENT_OPERATING_DATA(ATMOSPHERIC_PRESSURE_TOWERS, null, "recent_history_info", "Recent Operating Data", "distillation_point_prediction", "Distillation point prediction"),
    THE_CURRENT_OPERATING_CONDITION(ATMOSPHERIC_PRESSURE_TOWERS, null, "current_run_info", "Current Operating Condition", "path_plan", "Path planning");

    private final SubCategoryEnum categoryType;
    private final TagHistoryCategory tagHistoryCategory;
    private final String categoryIdentifier;
    private final String categoryName;
    private final String associatedCapability;
    private final String capabilityDescription;

    public static FileCategoryAbilityAssociation getCategoryByIdentifier(String identifier) {
        for (FileCategoryAbilityAssociation value : values()) {
            if (value.getCategoryIdentifier() != null && value.categoryIdentifier.equals(identifier)) {
                return value;
            }
        }
        return null;
    }

    public static String getCategoryNameBySubCategoryCode(List<Integer> codes) {
        List<SubCategoryEnum> subCategoryEnums = Arrays.stream(SubCategoryEnum.values())
            .filter(item -> codes.contains(item.getCode()))
            .collect(Collectors.toList());
        return Arrays.stream(values())
            .filter(item -> subCategoryEnums.contains(item.getCategoryType()))
            .map(FileCategoryAbilityAssociation::getCategoryName)
            .collect(Collectors.joining(","));
    }

    public static String getCategoryIdentifierBySubCategoryCode(List<Integer> codes) {
        List<SubCategoryEnum> subCategoryEnums = Arrays.stream(SubCategoryEnum.values())
            .filter(item -> codes.contains(item.getCode()))
            .collect(Collectors.toList());
        return Arrays.stream(values())
            .filter(item -> subCategoryEnums.contains(item.getCategoryType()))
            .map(FileCategoryAbilityAssociation::getCategoryIdentifier)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }

    public static String getCategoryIdentifierByTagHistoryCode(List<Integer> codes) {
        List<TagHistoryCategory> tagHistoryCategoryEnums = Arrays.stream(TagHistoryCategory.values())
            .filter(item -> codes.contains(item.getCode()))
            .collect(Collectors.toList());
        return Arrays.stream(values())
            .filter(item -> tagHistoryCategoryEnums.contains(item.getTagHistoryCategory()))
            .map(FileCategoryAbilityAssociation::getCategoryIdentifier)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }

    public static String getAbilityByTagHistoryCategory(TagHistoryCategory tagHistoryCategory) {
        if (tagHistoryCategory == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> tagHistoryCategory.equals(item.getTagHistoryCategory()))
            .map(FileCategoryAbilityAssociation::getAssociatedCapability)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }

    public static String getAbilityBySubCategory(SubCategoryEnum subCategoryEnum) {
        if (subCategoryEnum == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> subCategoryEnum.equals(item.getCategoryType()))
            .map(FileCategoryAbilityAssociation::getAssociatedCapability)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }

    public static String getAbilityDescriptionByAbility(List<String> abilities) {
        return Arrays.stream(values())
            .filter(item -> abilities.contains(item.getAssociatedCapability()))
            .map(FileCategoryAbilityAssociation::getCapabilityDescription)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(","));
    }
}

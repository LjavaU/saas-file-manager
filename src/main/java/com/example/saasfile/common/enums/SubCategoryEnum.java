package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum SubCategoryEnum {
    IDENTIFICATION_MODEL_INFO(0, "Identification Model Info"),
    DATA_SOURCE_CONFIG_INFO(1, "Data Source Config Info"),
    MATERIAL_PARAMETER_PROPERTY(2, "Material Parameter Property"),
    MATERIAL_PARAMETER_EQUATION(3, "Material Parameter Equation"),
    MATERIAL_BALANCE_SHEET(4, "Material Balance Sheet"),
    EQUIPMENT_INFO(5, "Equipment Info"),
    WORKING_CONDITION_INFO(6, "Working Condition Info"),
    TAG_CONFIGURATION(7, "Tag Configuration"),
    LOOP_CONFIGURATION(8, "Loop Configuration"),
    ALARM_TAG_MAPPING_TABLE(9, "Alarm Tag Mapping Table"),
    DYNAMIC_EQUIPMENT_INFO_MODEL(10, "Dynamic Equipment Info Model"),
    TAG_HISTORICAL_DATA(11, "Tag Historical Data"),
    HEAT_EXCHANGER_NETWORK_DESIGN_INFO(12, "Heat Exchanger Network Design Info"),
    OPERATING_PROCEDURE_DEVICE(13, "Operating Procedure Device"),
    PROCESS_INDEX_COMPOSITION(14, "Process Index Composition"),
    DCS_POINT_TABLE_FB(15, "DCS Point Table FB"),
    EARLY_WARNING_PROCESS_ABNORMALITIES(16, "Early Warning Process Abnormalities"),
    METRICS_BUSINESS_REPORT_DATA(17, "Metrics Business Report Data"),
    EXAM_QUESTION_DATA(18, "Exam Question Data"),
    OVERALL_INFORMATION_DEVICE(19, "Overall Information Device"),
    CONDITION_INPUT(20, "Condition Input"),
    ATMOSPHERIC_PRESSURE_TOWERS(21, "Atmospheric Pressure Towers"),
    KEY_PARAMETERS(22, "Key Parameters");

    private final Integer code;
    private final String description;

    public static String fromCode(int code) {
        for (SubCategoryEnum item : values()) {
            if (item.code == code) {
                return item.description;
            }
        }
        return null;
    }

    public static SubCategoryEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElse(null);
    }

    public static String getDescriptionByCodes(List<Integer> codes) {
        return codes.stream()
            .map(SubCategoryEnum::getByCode)
            .filter(Objects::nonNull)
            .map(SubCategoryEnum::getDescription)
            .collect(Collectors.joining(","));
    }
}

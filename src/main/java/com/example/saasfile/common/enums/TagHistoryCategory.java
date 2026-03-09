package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum TagHistoryCategory {
    CIRCUIT_HISTORY_DATA(0, "Circuit History Data"),
    TAG_HISTORY_DATA(1, "Tag History Data"),
    VARIABLE_HISTORY_DATA(2, "Variable History Data"),
    OPTIMIZATION_HISTORY_DATA(3, "Optimization History Data"),
    ALARM_PREDICTION_HISTORY_DATA(4, "Alarm Prediction History Data"),
    HEAT_EXCHANGER_NETWORK_OPERATION_INFO(5, "Heat Exchanger Network Operation Info"),
    TAG_DATA_FILE(6, "Tag Data File"),
    CIRCUIT_DATA_FILE(7, "Circuit Data File"),
    ANOMALY_DETECTION_HISTORICAL_DATA(8, "Anomaly Detection Historical Data"),
    BOILER_HISTORICAL_DATA(9, "Boiler Historical Data");

    private final Integer code;
    private final String category;

    public static String getCategoryByCode(List<Integer> codes) {
        return Arrays.stream(values())
            .filter(item -> codes.contains(item.code))
            .map(TagHistoryCategory::getCategory)
            .collect(Collectors.joining(","));
    }

    public static TagHistoryCategory getByCode(Integer code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElse(null);
    }

    public static String getCategoryByCode(Integer code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equals(code))
            .map(TagHistoryCategory::getCategory)
            .findFirst()
            .orElse(null);
    }
}

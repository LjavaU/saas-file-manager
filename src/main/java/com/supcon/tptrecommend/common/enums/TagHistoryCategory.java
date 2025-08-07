package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 位号历史值数据类型枚举。
 *
 * @author luhao
 * @since 2025/08/05 13:43:21
 */
@Getter
@AllArgsConstructor
public enum TagHistoryCategory {
    /**
     * 回路历史数据
     */
    CIRCUIT_HISTORY_DATA(0, "回路历史数据"),

    /**
     * 位号历史数据
     */
    TAG_HISTORY_DATA(1, "位号历史数据"),

    /**
     * 变量历史数据
     */
    VARIABLE_HISTORY_DATA(2, "变量历史数据"),

    /**
     * 优化历史数据
     */
    OPTIMIZATION_HISTORY_DATA(3, "优化历史数据"),

    /**
     * 预警预测历史数据
     */
    ALARM_PREDICTION_HISTORY_DATA(4, "预警预测历史数据"),

    /**
     * 换热网络运行信息
     */
    HEAT_EXCHANGER_NETWORK_OPERATION_INFO(5, "换热网络运行信息"),

    /**
     * 位号数据文件
     */
    TAG_DATA_FILE(6, "位号数据文件"),

    /**
     * 回路数据文件
     */
    CIRCUIT_DATA_FILE(7, "回路数据文件");

    private final Integer code;
    private final String category;

    /**
     * 根据code获取类别
     *
     * @param codes 编码
     * @return {@link String }
     * @author luhao
     * @since 2025/08/06 13:26:26
     *
     *
     */
    public static String getCategoryByCode(List<Integer> codes) {
            return Arrays.stream(TagHistoryCategory.values())
                .filter(item -> codes.contains(item.code))
                .map(TagHistoryCategory::getCategory)
                .collect(Collectors.joining(","));

    }

    // 根据code获取枚举
    public static TagHistoryCategory getByCode(Integer code) {
        return Arrays.stream(TagHistoryCategory.values())
            .filter(item -> item.code.equals(code))
            .findFirst()
            .orElse(null);
    }


}
package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类别枚举
 *
 * @author luhao
 * @since 2025/07/30 09:37:24
 */
@AllArgsConstructor
@Getter
public enum FileCategory {
    SYSTEM(0, "系统配置"),
    BASIC_DATA(1, "基础数据"),
    DYNAMIC_DATA(2, "动态数据"),
    BUSINESS_DATA(3, "业务数据");

    private final Integer code;
    private final String value;

    // 根据code获取value
    public static String getValueByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileCategory category : FileCategory.values()) {
            if (category.code.equals(code)) {
                return category.value;
            }
        }
        return null;
    }
}
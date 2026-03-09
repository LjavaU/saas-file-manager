package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileCategory {
    SYSTEM(0, "System"),
    BASIC_DATA(1, "Basic Data"),
    DYNAMIC_DATA(2, "Dynamic Data"),
    BUSINESS_DATA(3, "Business Data");

    private final Integer code;
    private final String value;

    public static String getValueByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileCategory category : values()) {
            if (category.code.equals(code)) {
                return category.value;
            }
        }
        return null;
    }
}

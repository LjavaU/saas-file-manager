package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 位号数据类型枚举
 *
 * @author luhao
 * @since 2025/07/15 16:39:35
 */
@Getter
@AllArgsConstructor
public enum TagDataTypeEnum {
    UNKNOWN("UNKNOW", 0),
    BOOLEAN("BOOLEAN", 1),
    S_BYTE("S_BYTE", 2),
    BYTE("BYTE", 3),
    SHORT("SHORT_INT", 4),
    U_SHORT("U_SHORT_INT", 5),
    INT("INT", 6),
    U_INT("U_INT", 7),
    LONG("LONG", 8),
    U_LONG("U_LONG", 9),
    FLOAT("FLOAT", 10),
    DOUBLE("DOUBLE", 11),
    DATETIME("DATETIME", 13),
    STRING("STRING", 12),
    CHAR("CHAR", 16),
    DECIMAL("DECIMAL", 18);

    private final String type;
    private final Integer code;

    /**
     * 根据type查询code
     * @param type 类型
     * @return {@link Integer }
     * @author luhao
     * @since 2025/07/15 16:43:07
     */
    public static Integer fromType(String type) {
        for (TagDataTypeEnum value : TagDataTypeEnum.values()) {
            if (value.type.equalsIgnoreCase(type)) {
                return value.code;
            }
        }
        return UNKNOWN.code;
    }
}

package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件夹类型枚举
 *
 * @author luhao
 * @since 2025/09/19 09:36:11
 */
@AllArgsConstructor
@Getter
public enum FolderTypeEnum {
    PERSONAL("personal", "个人文件夹"),
    SHARED("shared", "统一租户下共享文件夹")
    ;
    private final String code;
    private final String description;
}

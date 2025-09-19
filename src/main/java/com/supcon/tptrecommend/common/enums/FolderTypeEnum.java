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
    PERSONAL("private", "个人文件夹"),
    TENANT("tenant", "同一租户下共享")
    ;
    private final String code;
    private final String description;
}

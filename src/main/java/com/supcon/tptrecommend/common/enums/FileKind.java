package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型
 *
 * @author luhao
 * @since 2025/07/03 14:57:45
 */
@Getter
@AllArgsConstructor
public enum FileKind {
    FILE("file"),
    FOLDER("folder");

    private final String value;

}
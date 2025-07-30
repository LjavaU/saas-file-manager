package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件解析状态
 *
 * @author luhao
 * @since 2025/07/30 09:36:55
 */
@AllArgsConstructor
@Getter
public enum FileStatus {
    UNPARSED(0, "未解析"),
    PARSED(1, "解析完成"),
    PARSE_FAILED(2, "解析失败");

    private final Integer value;
    private final String desc;
}

package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * File parse/upload status.
 *
 * <p>Keep numeric codes backward-compatible with existing data.</p>
 *
 * @author luhao
 * @since 2025/07/30 09:36:55
 */
@AllArgsConstructor
@Getter
public enum FileStatus {
    UNPARSED(0, "unparsed"),
    PARSED(1, "parsed"),
    PARSE_FAILED(2, "parse_failed"),
    PARSE_NOT_SUPPORT(3, "parse_not_support"),
    UPLOADING(4, "uploading");

    private final Integer value;
    private final String desc;
}

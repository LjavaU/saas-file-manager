package com.supcon.tptrecommend.common.utils;

import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class DateParserUtil {

    // 更新后的格式列表，加入了对新格式的支持
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
        // 常见格式
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
        /*主要格式Begin*/
        DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/d H:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/d HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/M/dd H:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/M/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss"),
        /*主要格式 END*/

        // 带 'T' 的标准格式
        DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    /**
     * 将任意已知格式的字符串转换为 LocalDateTime
     *
     * @param dateString 日期时间字符串
     * @return 转换后的 LocalDateTime，如果所有格式都不匹配则返回 Optional.empty()
     */
    public static LocalDateTime parse(String dateString) {
        if (StrUtil.isBlank(dateString)) {
            return null;
        }

        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // 如果失败，尝试下一种格式
            }
        }
        // 如果所有格式都尝试失败，则返回空
        return null;
    }

}
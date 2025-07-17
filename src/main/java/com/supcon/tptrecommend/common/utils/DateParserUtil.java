package com.supcon.tptrecommend.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateParserUtil {

    // 使用 DateTimeFormatterBuilder 构建一个能处理多种格式的、高效的解析器
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
        // 将常见格式作为可选格式添加
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/d H:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/d HH:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/dd H:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/dd HH:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
        // 添加对ISO标准格式（带'T'）的支持
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .toFormatter(); // 构建最终的 Formatter

    /**
     * 使用单一、高效的解析器将字符串转换为 LocalDateTime
     *
     * @param dateString 日期时间字符串
     * @return 转换后的 LocalDateTime，如果格式不匹配则返回 null
     */
    public static LocalDateTime parse(String dateString) {
        if (StrUtil.isBlank(dateString)) {
            return null;
        }
        try {
            // 直接使用这个万能解析器进行解析
            return LocalDateTime.parse(dateString, FLEXIBLE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("无法解析日期字符串: {}", dateString);
            return null;
        }
    }

}
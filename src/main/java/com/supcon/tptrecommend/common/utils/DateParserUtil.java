package com.supcon.tptrecommend.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateParserUtil {

    /**
     * 辅助方法：构建一个带可选纳秒部分的DateTimeFormatter
     * @param pattern 基础的日期时间模式
     * @return 一个增强的、支持可选纳秒的DateTimeFormatter
     */
    private static DateTimeFormatter buildFormatterWithOptionalNanos(String pattern) {
        return new DateTimeFormatterBuilder()
            .appendPattern(pattern)
            .optionalStart()
            .appendPattern(".n")
            .optionalEnd()
            .toFormatter();
    }

    /**
     * 使用 DateTimeFormatterBuilder 构建一个能处理多种格式的、高效的解析器
     */
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
        // 1. 支持 yyyy-M-d... 格式 (单位数/双位数月、日、时)，及其可选的纳秒
        .appendOptional(buildFormatterWithOptionalNanos("yyyy-M-d H:mm:ss"))
        // 不带秒的格式
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d H:mm"))
        // 2. 支持 yyyy/M/d... 格式 (单位数/双位数月、日、时)，及其可选的纳秒
        .appendOptional(buildFormatterWithOptionalNanos("yyyy/M/d H:mm:ss"))
        // 不带秒的格式
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:mm"))
        // 3. 支持 yyyy.M.d... 格式
        .appendOptional(buildFormatterWithOptionalNanos("yyyy.M.d H:mm:ss"))
        // 4. 支持 ISO 标准格式
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .toFormatter();

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
package com.example.saasfile.common.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

@Slf4j
public class DateParserUtil {

    
    private static DateTimeFormatter buildFormatterWithOptionalNanos(String pattern) {
        return new DateTimeFormatterBuilder()
            .appendPattern(pattern)
            .optionalStart()
            .appendPattern(".n")
            .optionalEnd()
            .toFormatter();
    }

    
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
        .appendOptional(buildFormatterWithOptionalNanos("yyyy-M-d H:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d H:mm"))
        .appendOptional(buildFormatterWithOptionalNanos("yyyy/M/d H:mm:ss"))
        .appendOptional(DateTimeFormatter.ofPattern("yyyy/M/d H:mm"))
        .appendOptional(buildFormatterWithOptionalNanos("yyyy.M.d H:mm:ss"))
        .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .toFormatter();

    
    public static LocalDateTime parse(String dateString) {
        if (StrUtil.isBlank(dateString)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, FLEXIBLE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
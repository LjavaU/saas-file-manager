package com.supcon.tptrecommend.common.utils;

public class BooleanValidator {

    /**
     * 严格检查字符串是否为 "true" 或 "false" (忽略大小写)
     *
     * @param s The string to check.
     * @return true if the string is a case-insensitive match for "true" or "false", false otherwise.
     */
    public static boolean isBooleanString(String s) {
        if (s == null) {
            return false;
        }
        return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
    }
}
package com.example.saasfile.common.utils;

public class BooleanValidator {

    /**
     * 涓ユ牸妫€鏌ュ瓧绗︿覆鏄惁涓?"true" 鎴?"false" (蹇界暐澶у皬鍐?
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
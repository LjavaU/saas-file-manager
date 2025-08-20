package com.supcon.tptrecommend.common.utils;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.security.SecureRandom;

public final class ShortIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    // 定义我们希望在ID中使用的字符集
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    
    /**
     * 生成一个指定长度的、URL安全的随机ID
     * @param length 推荐 8 位或以上
     * @return 随机ID字符串
     */
    public static String generate(int length) {
        return NanoIdUtils.randomNanoId(RANDOM, ALPHABET, length);
    }
    
    public static void main(String[] args) {
        System.out.println(generate(6));
    }
}
package com.example.saasfile.common.utils;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.security.SecureRandom;

public final class ShortIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    
    
    public static String generate(int length) {
        return NanoIdUtils.randomNanoId(RANDOM, ALPHABET, length);
    }
    
    public static void main(String[] args) {
        System.out.println(generate(6));
    }
}
package com.supcon.tptrecommend.common.utils;

import java.util.Random;

public class RandomNumberUtil {
    public static int getRandomNumberInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }


}

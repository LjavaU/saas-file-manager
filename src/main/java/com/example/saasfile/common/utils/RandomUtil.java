package com.example.saasfile.common.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    public static int getRandomPercentage(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max");
        }
        return ThreadLocalRandom.current().nextInt(min + 1, max + 1);
    }
}

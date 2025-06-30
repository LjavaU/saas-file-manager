package com.supcon.tptrecommend.common.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    /**
     * 生成一个在 [min, max] 范围内的随机整数 (包含 min 和 max)
     *
     * @param min 范围起始值
     * @param max 范围结束值
     * @return 随机整数
     */
    public static int getRandomPercentage(int min, int max) {
        // 参数校验，确保 min <= max
        if (min > max) {
            throw new IllegalArgumentException("起始值不能大于结束值 (min > max)");
        }
        // 这个方法的范围是 (origin, bound]
        return ThreadLocalRandom.current().nextInt(min + 1, max + 1);
    }

}
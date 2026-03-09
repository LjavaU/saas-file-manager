package com.example.saasfile.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 鐭ヨ瘑搴撹В鏋愮姸鎬佹灇涓?
 *
 * @author luhao
 * @since 2025/07/30 09:37:55
 */
@AllArgsConstructor
@Getter
public enum KnowledgeParseState {
    /**
     * 姝ｅ湪涓婁紶鐘舵€?
     */
    GRAY(0, "gray"),
    /**
     * split鎴杄mbedding澶辫触
     */
    RED(1, "red"),
    /**
     * milvus鎻掑叆澶辫触
     */
    YELLOW(2, "yellow"),
    /**
     * 涓婁紶鎴愬姛
     */
    GREEN(3, "green");

    private final Integer value;
    private final String desc;

    // 鏍规嵁desc鑾峰彇value
    public static Integer valueByDesc(String desc) {
        for (KnowledgeParseState state : KnowledgeParseState.values()) {
            if (state.desc.equals(desc)) {
                return state.value;
            }
        }
        return null;
    }
}

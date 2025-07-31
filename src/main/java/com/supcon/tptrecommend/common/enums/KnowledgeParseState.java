package com.supcon.tptrecommend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识库解析状态枚举
 *
 * @author luhao
 * @since 2025/07/30 09:37:55
 */
@AllArgsConstructor
@Getter
public enum KnowledgeParseState {
    /**
     * 正在上传状态
     */
    GRAY(0, "gray"),
    /**
     * split或embedding失败
     */
    RED(1, "red"),
    /**
     * milvus插入失败
     */
    YELLOW(2, "yellow"),
    /**
     * 上传成功
     */
    GREEN(3, "green");

    private final Integer value;
    private final String desc;

    // 根据desc获取value
    public static Integer valueByDesc(String desc) {
        for (KnowledgeParseState state : KnowledgeParseState.values()) {
            if (state.desc.equals(desc)) {
                return state.value;
            }
        }
        return null;
    }
}

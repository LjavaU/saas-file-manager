package com.supcon.tptrecommend.manager.strategy;

import java.util.Set;

public interface FileAnalysisHandle {

    /**
     * 获取此策略支持的所有文件类型（文件后缀）。
     * @return 支持的文件类型后缀集合 (例如, "JPG", "PNG")
     * @author luhao
     * @since 2025/06/13 09:35:11
     */
    Set<String> getSupportedTypes();

    /**
     * 处理文件分析
     *
     * @param fileId   文件 ID
     * @author luhao
     * @since 2025/06/18 20:05:43
     */
    void handleFileAnalysis(Long fileId);
}

package com.supcon.tptrecommend.manager;

import java.util.Set;

public interface FileAnalysisHandle {

    /**
     * 处理文件分析
     *
     * @param bytes  字节
     * @param fileId 文件 ID
     * @author luhao
     * @since 2025/06/13 09:34:59
     */
    void handleFileAnalysis(byte[] bytes, Long fileId);

    /**
     * 获取此策略支持的所有文件类型（文件后缀）。
     * @return 支持的文件类型后缀集合 (例如, "JPG", "PNG")
     * @author luhao
     * @since 2025/06/13 09:35:11
     */
    Set<String> getSupportedTypes();
}

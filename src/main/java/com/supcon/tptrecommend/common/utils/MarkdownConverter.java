package com.supcon.tptrecommend.common.utils;

import java.util.List;

/**
 * Markdown 转换器
 * 用于将表格数据转换为Markdown格式
 *
 * @author luhao
 * @date 2025/06/09 16:00:52
 */
public class MarkdownConverter {


    /**
     * 生成 Markdown 表
     *
     * @param data 数据
     * @return {@link String } 返回Markdown字符串
     * @author luhao
     * @since 2025/06/09 15:58:10
     */
    public static String generateMarkdownTable(List<List<String>> data) {
        // --- 1. 输入验证 ---
        if (data == null || data.isEmpty()) {
            return "";
        }

        // --- 2. 确定表格的最大列数以处理不规则行 ---
        int maxColumns = 0;
        for (List<String> row : data) {
            if (row != null && row.size() > maxColumns) {
                maxColumns = row.size();
            }
        }
        if (maxColumns == 0) {
            return "";
        }

        StringBuilder markdownBuilder = new StringBuilder();

        // --- 3. 构建表头 (将输入的第一行作为表头) ---
        appendMarkdownRow(markdownBuilder, data.get(0), maxColumns);

        // --- 4. 构建分隔线 ---
        markdownBuilder.append("|");
        for (int i = 0; i < maxColumns; i++) {
            markdownBuilder.append(" --- |");
        }
        markdownBuilder.append("\n");

        // --- 5. 构建所有数据行 (从输入的第二行开始) ---
        for (int i = 1; i < data.size(); i++) {
            appendMarkdownRow(markdownBuilder, data.get(i), maxColumns);
        }

        return markdownBuilder.toString();
    }

    /**
     * 辅助方法：格式化并追加单行到 Markdown 字符串。
     * @param builder 用于构建的 StringBuilder
     * @param rowData 当前行的数据
     * @param maxColumns 表格最大列数
     */
    private static void appendMarkdownRow(StringBuilder builder, List<String> rowData, int maxColumns) {
        builder.append("|");
        for (int i = 0; i < maxColumns; i++) {
            String cellContent = (rowData != null && i < rowData.size() && rowData.get(i) != null) ? rowData.get(i) : "";

            // **核心处理逻辑：清理和转义内容**
            String sanitizedContent = sanitizeCellContent(cellContent);

            builder.append(" ").append(sanitizedContent).append(" |");
        }
        builder.append("\n");
    }

    /**
     * 清理并转义单元格内容以符合 Markdown 规范。
     * @param content 原始单元格内容
     * @return 清理和转义后的内容
     */
    private static String sanitizeCellContent(String content) {
        if (content == null) {
            return "";
        }
        // 1. 去除首尾多余的空白字符
        String trimmedContent = content.trim();
        // 2. 将管道符 | 转义为 \|
        String escapedPipe = trimmedContent.replace("|", "\\|");
        // 3. 将换行符 \n 替换为 <br>
        return escapedPipe.replace("\n", "<br>");
    }


}
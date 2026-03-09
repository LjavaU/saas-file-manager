package com.example.saasfile.common.utils;

import java.util.List;


public class MarkdownConverter {


    
    public static String generateMarkdownTable(List<List<String>> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        int maxColumns = 0;
        for (List<String> row : data) {
            if (row != null && row.size() > maxColumns) {
                maxColumns = row.size();
            }
        }
        if (maxColumns == 0) {
            return null;
        }

        StringBuilder markdownBuilder = new StringBuilder();
        appendMarkdownRow(markdownBuilder, data.get(0), maxColumns);
        markdownBuilder.append("|");
        for (int i = 0; i < maxColumns; i++) {
            markdownBuilder.append(" --- |");
        }
        markdownBuilder.append("\n");
        for (int i = 1; i < data.size(); i++) {
            appendMarkdownRow(markdownBuilder, data.get(i), maxColumns);
        }

        return markdownBuilder.toString();
    }

    
    private static void appendMarkdownRow(StringBuilder builder, List<String> rowData, int maxColumns) {
        builder.append("|");
        for (int i = 0; i < maxColumns; i++) {
            String cellContent = (rowData != null && i < rowData.size() && rowData.get(i) != null) ? rowData.get(i) : "";
            String sanitizedContent = sanitizeCellContent(cellContent);

            builder.append(" ").append(sanitizedContent).append(" |");
        }
        builder.append("\n");
    }

    
    private static String sanitizeCellContent(String content) {
        if (content == null) {
            return "";
        }
        String trimmedContent = content.trim();
        String escapedPipe = trimmedContent.replace("|", "\\|");
        return escapedPipe.replace("\n", "<br>");
    }


}
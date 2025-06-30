package com.supcon.tptrecommend.common.utils;

import java.text.DecimalFormat;

public class FileSizeFormatter {

    /**
     * 设置文件大小格式
     * 根据文件大小，动态选择单位
     *
     * @param size 大小
     * @return {@link String }
     * @author luhao
     * @since 2025/06/27 10:59:51
     */
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        // 计算文件大小的对数，以确定其所在的单位级别
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        // 使用 DecimalFormat 来格式化数字，保留两位小数
        DecimalFormat df = new DecimalFormat("###0.##");

        return df.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }




}


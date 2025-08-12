package com.supcon.tptrecommend.common;

public class Constants {

    public static final String DEFAULT_TENANT_ID = "0";

    /**
     * 从excel或者csv文件中读取的行数阀值，超过这个值则使用批量插入
     */
    public static final int INSERT_SIZE = 1000;

    /**
     * 位号历史价值插入大小
     */
    public static final int TAG_HISTORY_VALUE_INSERT_SIZE = 9000;

    /**
     * 指标解析任务redis key
     */
    public  static final String INDEX_PARSE_TASK = "file_parse_index_task";
}

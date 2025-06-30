package com.supcon.tptrecommend.common;

public class Constants {

    public static final String DEFAULT_TENANT_ID = "0";

    /**
     * 从excel或者csv文件中读取的行数阀值，超过这个值则使用批量插入
     */
    public static final int READ_BATCH_SIZE = 1000;
}

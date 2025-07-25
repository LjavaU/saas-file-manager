package com.supcon.tptrecommend.feign.entity.index;

import lombok.Data;

/**
 * 文件上传结果的数据模型，用于封装单个文件的上传和解析状态
 */
@Data
public class UploadResultItem {

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 文件id, 同一文件重复上传文件id不一样
     * 示例："20cca855-9ce4-4e2e-8078-9a404d4b6c5a.xlsx"
     */
    private String fileId;

    /**
     * 原始文件名（包含扩展名）
     * 示例："data.xlsx"
     */
    private String fileName;

    /**
     * 文件大小（单位：字节）
     * 示例：1024（表示1KB）
     */
    private long fileSize;

    /**
     * 上传状态
     * true：上传成功
     * false：上传失败
     */
    private Boolean uploadStatus;

    /**
     * 上传结果消息
     * 成功时：可选的成功描述（如"上传完成"）
     * 失败时：具体的错误信息（如"文件格式不支持"）
     */
    private String uploadMessage;


    /**
     * 文件存储路径（服务器端）
     * 示例："/uploads/temp/data_20231025.xlsx"
     */
    private String filePath;
}

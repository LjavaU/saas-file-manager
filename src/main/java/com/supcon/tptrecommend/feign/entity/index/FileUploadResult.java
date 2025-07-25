package com.supcon.tptrecommend.feign.entity.index;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "文件上传返回结果")
public class FileUploadResult {

    // 上传的文件总数
    private int totalFiles;
    // 每个文件的处理结果列表
    private List<UploadResultItem> items;
    // 处理摘要信息（可选）
    private String summary;
}

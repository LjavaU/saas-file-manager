package com.supcon.tptrecommend.dto.fileUpload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
public class ExcelUploadRequest {

    @NotBlank(message = "文件名称不能为空")
    private String fileName;
    @NotEmpty(message = "文件内容不能为空")
    private Map<String, List<List<String>>> content;

}
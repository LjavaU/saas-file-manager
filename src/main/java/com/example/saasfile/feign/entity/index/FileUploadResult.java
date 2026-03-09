package com.example.saasfile.feign.entity.index;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "")
public class FileUploadResult {
    private int totalFiles;
    private List<UploadResultItem> items;
    private String summary;
}

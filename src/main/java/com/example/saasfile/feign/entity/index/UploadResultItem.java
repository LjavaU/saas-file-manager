package com.example.saasfile.feign.entity.index;

import lombok.Data;


@Data
public class UploadResultItem {

    
    private String taskId;

    
    private String fileId;

    
    private String fileName;

    
    private long fileSize;

    
    private Boolean uploadStatus;

    
    private String uploadMessage;


    
    private String filePath;
}

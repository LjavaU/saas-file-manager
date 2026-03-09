package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "")
public class MultipartUploadCompleteReq {

    @NotNull(message = "fileId is required")
    @ApiModelProperty(value = "")
    private Long fileId;

    @NotBlank(message = "uploadId is required")
    @ApiModelProperty(value = "")
    private String uploadId;
}

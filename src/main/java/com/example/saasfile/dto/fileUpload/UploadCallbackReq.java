package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "")
public class UploadCallbackReq {

    @NotNull(message = "fileId is required")
    @ApiModelProperty(value = "")
    private Long fileId;

    @NotBlank(message = "uploadId is required")
    @ApiModelProperty(value = "")
    private String uploadId;

    @Pattern(regexp = "^$|^[a-fA-F0-9]{32}$", message = "invalid MD5")
    @ApiModelProperty(value = "")
    private String fileMd5;

    @ApiModelProperty(value = "")
    private String etag;
}

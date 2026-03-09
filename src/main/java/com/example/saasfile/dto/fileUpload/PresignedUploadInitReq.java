package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "")
public class PresignedUploadInitReq {

    @NotBlank(message = "originalName is required")
    @Size(max = 255, message = "originalName is too long")
    @ApiModelProperty(value = "")
    private String originalName;

    @NotNull(message = "fileSize is required")
    @Min(value = 1, message = "fileSize must be greater than 0")
    @ApiModelProperty(value = "")
    private Long fileSize;

    @Size(max = 128, message = "contentType is too long")
    @ApiModelProperty(value = "")
    private String contentType;

    @Size(max = 256, message = "path is too long")
    @ApiModelProperty(value = "")
    private String path;

    @Pattern(regexp = "^$|^[a-fA-F0-9]{32}$", message = "fileMd5 must be a 32-character hex string")
    @ApiModelProperty(value = "")
    private String fileMd5;
}

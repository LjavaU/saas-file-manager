package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "")
public class MultipartUploadInitReq {

    @NotBlank(message = "originalName is required")
    @Size(max = 255, message = "originalName is too long")
    @ApiModelProperty(value = "")
    private String originalName;

    @NotNull(message = "fileSize is required")
    @Min(value = 1, message = "fileSize must be positive")
    @ApiModelProperty(value = "")
    private Long fileSize;

    @NotNull(message = "totalParts is required")
    @Min(value = 1, message = "totalParts must be positive")
    @Max(value = 10000, message = "totalParts is too large")
    @ApiModelProperty(value = "")
    private Integer totalParts;

    @Size(max = 128, message = "contentType is too long")
    @ApiModelProperty(value = "")
    private String contentType;

    @Size(max = 256, message = "path is too long")
    @ApiModelProperty(value = "")
    private String path;

    @Pattern(regexp = "^$|^[a-fA-F0-9]{32}$", message = "invalid MD5")
    @ApiModelProperty(value = "")
    private String fileMd5;
}

package com.example.saasfile.dto.fileshare;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "")
public class FileShareRequest {

    @NotBlank(message = "bucketName is required")
    @ApiModelProperty(value = "")
    private String bucketName;

    @NotBlank(message = "objectName is required")
    @ApiModelProperty(value = "")
    private String objectName;

    @ApiModelProperty(value = "")
    private long expirationSecond = 86400;
}

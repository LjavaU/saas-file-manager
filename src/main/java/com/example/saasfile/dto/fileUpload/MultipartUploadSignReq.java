package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "")
public class MultipartUploadSignReq {

    @NotNull(message = "fileId is required")
    @ApiModelProperty(value = "")
    private Long fileId;

    @NotBlank(message = "uploadId is required")
    @ApiModelProperty(value = "")
    private String uploadId;

    @NotNull(message = "partNumber is required")
    @Min(value = 1, message = "partNumber must be greater than or equal to 1")
    @Max(value = 10000, message = "partNumber must be less than or equal to 10000")
    @ApiModelProperty(value = "")
    private Integer partNumber;
}

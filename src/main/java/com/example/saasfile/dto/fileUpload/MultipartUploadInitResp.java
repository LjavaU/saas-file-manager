package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class MultipartUploadInitResp {

    @ApiModelProperty(value = "")
    private Long fileId;

    @ApiModelProperty(value = "")
    private String bucketName;

    @ApiModelProperty(value = "")
    private String objectName;

    @ApiModelProperty(value = "")
    private String uploadId;

    @ApiModelProperty(value = "")
    private Integer totalParts;

    @ApiModelProperty(value = "")
    private Long expireAt;
}

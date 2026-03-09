package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class MultipartUploadSignResp {

    @ApiModelProperty(value = "")
    private Integer partNumber;

    @ApiModelProperty(value = "")
    private String partObjectName;

    @ApiModelProperty(value = "")
    private String uploadUrl;

    @ApiModelProperty(value = "")
    private Long expireAt;
}

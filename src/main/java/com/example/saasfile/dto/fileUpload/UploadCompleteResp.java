package com.example.saasfile.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class UploadCompleteResp {

    @ApiModelProperty(value = "")
    private Long fileId;

    @ApiModelProperty(value = "")
    private String objectName;

    @ApiModelProperty(value = "")
    private String etag;

    @ApiModelProperty(value = "")
    private Boolean parseTriggered;

    @ApiModelProperty(value = "")
    private Integer fileStatus;
}

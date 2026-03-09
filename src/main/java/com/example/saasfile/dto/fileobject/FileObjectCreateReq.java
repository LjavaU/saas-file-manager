package com.example.saasfile.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;


@Data
@Builder
@ApiModel(description = "")
public class FileObjectCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Long userId;

    @ApiModelProperty(value = "")
    private String userName;

    @ApiModelProperty(value = "")
    private String objectName;

    @ApiModelProperty(value = "")
    private String originalName;

    @ApiModelProperty(value = "")
    private String bucketName;

    @ApiModelProperty(value = "")
    private String contentType;

    @ApiModelProperty(value = "")
    private Long fileSize;

    @ApiModelProperty(value = "")
    private String tags;

}
package com.example.saasfile.dto.fileobject;

import com.example.saasfile.support.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "")
public class FileObjectResp extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private String tenantId;

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
    private String fileSize;

    @ApiModelProperty(value = "")
    private String category;

    @ApiModelProperty(value = "")
    private String firstCategory;


    @ApiModelProperty(value = "")
    private String secondCategory;


    @ApiModelProperty(value = "")
    private String ability;


    @ApiModelProperty(value = "")
    private String contentOverview;

    @ApiModelProperty(value = "")
    private Integer fileStatus;

    @ApiModelProperty(value = "")
    private String categoryIdentifier;
}
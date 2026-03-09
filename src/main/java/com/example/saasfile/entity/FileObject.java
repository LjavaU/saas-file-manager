package com.example.saasfile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.saasfile.support.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_object")
@ApiModel(description = "")
public class FileObject extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private Long userId;

    @ApiModelProperty(value = "")
    private String tenantId;

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
    private String category;


    @ApiModelProperty(value = "")
    private String ability;


    @ApiModelProperty(value = "")
    private String contentOverview;

    @ApiModelProperty(value = "")
    private Integer fileStatus;

    @ApiModelProperty(value = "")
    private String subCategory;

    @ApiModelProperty(value = "")
    private Integer knowledgeParseState;

    @ApiModelProperty(value = "")
    private String thirdLevelCategory;
}
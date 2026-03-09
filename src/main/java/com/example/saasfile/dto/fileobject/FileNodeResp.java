package com.example.saasfile.dto.fileobject;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "")
public class FileNodeResp {


    @ApiModelProperty(value = "")
    private Long id;

    @ApiModelProperty(value = "")
    private String name;

    @ApiModelProperty(value = "")
    private String path;

    @ApiModelProperty(value = "")
    private String type;

    @ApiModelProperty(value = "")
    private String folderType;

    @ApiModelProperty(value = "")
    private String size;

    @ApiModelProperty(value = "")
    private String tenantId;

    @ApiModelProperty(value = "")
    private Long userId;

    @ApiModelProperty(value = "")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "")
    private String category;


    @ApiModelProperty(value = "")
    private String ability;


    @ApiModelProperty(value = "")
    private String contentOverview;

    @ApiModelProperty(value = "")
    private Integer fileStatus;

    @ApiModelProperty(value = "")
    private Integer fileCount;

    @ApiModelProperty(value = "")
    private List<String> questions;

}
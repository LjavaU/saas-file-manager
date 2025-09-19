package com.supcon.tptrecommend.dto.fileobject;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "返回文件层级返回体")
public class FileNodeResp {


    @ApiModelProperty(value = "文件/夹Id")
    private Long id;

    @ApiModelProperty(value = "文件/夹名称")
    private String name;

    @ApiModelProperty(value = "文件路径")
    private String path;

    @ApiModelProperty(value = "文件类型, \"folder\" or \"file\"")
    private String type;

    @ApiModelProperty(value = "文件夹类型",example = "personal:个人文件夹 shared:共享文件夹")
    private String folderType;

    @ApiModelProperty(value = "文件大小")
    private String size;

    @ApiModelProperty(value = "租户 ID")
    private String tenantId;

    @ApiModelProperty(value = "用户 ID")
    private Long userId;

    @ApiModelProperty(value = "上传时间")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "所属分类")
    private String category;


    @ApiModelProperty(value = "对应能力&应用")
    private String ability;


    @ApiModelProperty(value = "内容概述")
    private String contentOverview;

    @ApiModelProperty(value = "文件解析状态，【0-未解析，1-解析完成，2-解释失败】")
    private Integer fileStatus;

    @ApiModelProperty(value = "文件夹下的文件数量")
    private Integer fileCount;

    @ApiModelProperty(value = "推荐问题")
    private List<String> questions;

}
package com.supcon.tptrecommend.dto.filerecommendation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel( description = "文件推荐问题创建请求体")
public class FileRecommendationCreateReq {

    @ApiModelProperty(value = "租户 ID")
    private String tenantId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "关键词")
    private String keyword;

    @ApiModelProperty(value = "问题集")
    private String questions;
}

package com.supcon.tptrecommend.dto.filerecommendation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FileRecommendationResp {

    private Long id;

    @ApiModelProperty(value = "租户id")
    private String tenantId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "关键词")
    private String keyword;

    @ApiModelProperty(value = "问题集")
    private String questions;
}

package com.supcon.tptrecommend.dto.filerecommendation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel( description = "文件推荐问题查询请求体")
@Builder
public class FileRecommendationReq {

    @ApiModelProperty(value = "租户 ID")
    private String tenantId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;
}

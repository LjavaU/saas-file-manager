package com.example.saasfile.dto.filerecommendation;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel(description = "")
@Builder
public class FileRecommendationReq {

    @ApiModelProperty(value = "")
    private String tenantId;

    @ApiModelProperty(value = "")
    private Long fileId;
}

package com.example.saasfile.dto.filerecommendation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FileRecommendationResp {

    private Long id;

    @ApiModelProperty(value = "")
    private String tenantId;

    @ApiModelProperty(value = "")
    private Long fileId;

    @ApiModelProperty(value = "")
    private String keyword;

    @ApiModelProperty(value = "")
    private String questions;
}

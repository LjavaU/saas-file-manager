package com.supcon.tptrecommend.dto.questionrecommend;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "首页推荐问题刷新请求体")
public class HomeQuestionRecommendReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位")
    private String post;

    @ApiModelProperty(value = "装置")
    private String device;
}

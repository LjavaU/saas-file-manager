package com.supcon.tptrecommend.feign.entity.datahub;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value = "数据创建模型", description = "数据创建模型")
public class TagInfoCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "系统位号名/位号名称")
    private String tagName;

    @ApiModelProperty(value = "底层位号名")
    private String tagBaseName;

    @ApiModelProperty(value = "位号描述")
    private String tagDesc;

    @NotNull(message = "位号类型：{}")
    private Integer tagType = 4;

    @ApiModelProperty(value = "位号工程单位/位号单位")
    private String unit;

    @ApiModelProperty(value = "位号数据类型")
    private Integer dataType;

    @ApiModelProperty(value = "底层位号数据类型")
    private Integer baseDataType;

    @ApiModelProperty(value = "高限/位号值上限/原始上限")
    private Object limitUp;

    @ApiModelProperty(value = "底限/位号值下限/原始下限")
    private Object limitDown;

    @ApiModelProperty(value = "高二限/位号值上上限/工程上限")
    private Object limitUpUp;

    @ApiModelProperty(value = "低二限/位号值下下限/工程下限")
    private Object limitDownDown;

    @ApiModelProperty(value = "高三限/位号值上上上限")
    private Object limitUpUpUp;

    @ApiModelProperty(value = "低三限/位号值下下下限")
    private Object limitDownDownDown;

    @ApiModelProperty(value = "是否只读")
    private Boolean onlyRead;
}
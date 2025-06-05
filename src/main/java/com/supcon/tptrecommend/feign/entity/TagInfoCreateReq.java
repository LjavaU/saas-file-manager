package com.supcon.tptrecommend.feign.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value = "数据创建模型", description = "数据创建模型")
public class TagInfoCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "系统位号名：{}")
    @Length(max = 255, message = "{系统位号名}：{}")
    private String tagName;

    @Length(max = 255, message = "{底层位号名}：{}")
    private String tagBaseName;

    @ApiModelProperty(value = "位号描述(最大255字符)", example = "位号描述")
    @Length(max = 255, message = "{位号描述}：{}")
    private String tagDesc;

    @NotNull(message = "位号类型：{}")
    @ApiModelProperty(value = "位号类型", required = true)
    private Integer tagType;

    @ApiModelProperty(value = "位号数据源id")
    private Long dsId;

    @ApiModelProperty(value = "位号工程单位(最大20字符)", example = "位号工程单位")
    @Length(max = 20, message = "{位号工程单位}：{}")
    private String unit;

    @ApiModelProperty(value = "位号数据类型")
    private Integer dataType;

    @ApiModelProperty(value = "底层位号数据类型")
    private Integer baseDataType;

    @ApiModelProperty(value = "二次位号取值表达式(最大1000字符)", example = "二次位号取值表达式")
    @Length(max = 1000, message = "{二次位号取值表达式}：{}")
    private String quaExpression;

    @ApiModelProperty(value = "平均值位号关联位号")
    private Integer avgDuration;

    @ApiModelProperty(value = "平均值位号时长")
    private Long avgRelatedTag;

    @ApiModelProperty(value = "平均值位号时间间隔")
    private Integer avgInterval;

    @ApiModelProperty(value = "平均值位号偏移量")
    private Integer avgOffset;

    @ApiModelProperty(value = "自定义位号值(最大5000字符)", example = "自定义位号值")
    @Length(max = 5000, message = "{自定义位号值}：{}")
    private String virtualValue;

    @ApiModelProperty(value = "位号值上限", example = "0")
    private Object limitUp;

    @ApiModelProperty(value = "位号值下限", example = "10")
    private Object limitDown;

    @ApiModelProperty(value = "是否只读", example = "true")
    private Boolean onlyRead;
}
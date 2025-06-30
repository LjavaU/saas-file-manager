package com.supcon.tptrecommend.feign.entity.datahub;

import com.supcon.system.base.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "数据响应模型", description = "数据响应模型")
public class TagInfoResp extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资源名", example = "tag001_001")
    private String name;

    @ApiModelProperty(value = "系统位号", example = "tag001_001")
    private String tagName;

    @ApiModelProperty(value = "底层位号", example = "tag001")
    private String tagBaseName;

    @ApiModelProperty(value = "位号描述", example = "位号一")
    private String tagDesc;

    @ApiModelProperty(value = "位号类型", required = true)
    private Integer tagType;

    @ApiModelProperty(value = "位号类型")
    private String tagTypeName;

    @ApiModelProperty(value = "位号数据源id")
    private Long dsId;

    @ApiModelProperty(value = "位号数据源名称")
    private String dsName;

    @ApiModelProperty(value = "位号工程单位", example = "位号工程单位")
    private String unit;

    @ApiModelProperty(value = "位号数据类型")
    private Integer dataType;

    @ApiModelProperty(value = "位号数据类型描述")
    private String dataTypeName;

    @ApiModelProperty(value = "底层位号数据类型")
    private Integer baseDataType;

    @ApiModelProperty(value = "创建者")
    private String createBy;

    @ApiModelProperty(value = "修改者")
    private String updateBy;

    @ApiModelProperty(value = "二次位号取值表达式", example = "二次位号取值表达式")
    private String quaExpression;

    @ApiModelProperty(value = "平均值关联位号")
    private Long avgRelatedTag;

    @ApiModelProperty(value = "平均值关联位号名称")
    private String avgRelatedTagName;

    @ApiModelProperty(value = "平均值位号时长")
    private Integer avgDuration;

    @ApiModelProperty(value = "平均值位号时间间隔")
    private Integer avgInterval;

    @ApiModelProperty(value = "平均值位号偏移量")
    private Integer avgOffset;

    @ApiModelProperty(value = "自定义位号值", example = "自定义位号值")
    private Object virtualValue;

    @ApiModelProperty(value = "位号值上限", example = "0")
    private Object limitUp;

    @ApiModelProperty(value = "位号值下限", example = "10")
    private Object limitDown;

    @ApiModelProperty(value = "是否只读", example = "true")
    private Boolean onlyRead;

    @ApiModelProperty(value = "位号值")
    private Object tagValue;
}
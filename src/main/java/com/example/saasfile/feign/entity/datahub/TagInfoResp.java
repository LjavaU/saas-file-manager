package com.example.saasfile.feign.entity.datahub;

import com.example.saasfile.support.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "")
public class TagInfoResp extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private String name;

    @ApiModelProperty(value = "")
    private String tagName;

    @ApiModelProperty(value = "")
    private String tagBaseName;

    @ApiModelProperty(value = "")
    private String tagDesc;

    @ApiModelProperty(value = "")
    private Integer tagType;

    @ApiModelProperty(value = "")
    private String tagTypeName;

    @ApiModelProperty(value = "")
    private Long dsId;

    @ApiModelProperty(value = "")
    private String dsName;

    @ApiModelProperty(value = "")
    private String unit;

    @ApiModelProperty(value = "")
    private Integer dataType;

    @ApiModelProperty(value = "")
    private String dataTypeName;

    @ApiModelProperty(value = "")
    private Integer baseDataType;

    @ApiModelProperty(value = "")
    private String createBy;

    @ApiModelProperty(value = "")
    private String updateBy;

    @ApiModelProperty(value = "")
    private String quaExpression;

    @ApiModelProperty(value = "")
    private Long avgRelatedTag;

    @ApiModelProperty(value = "")
    private String avgRelatedTagName;

    @ApiModelProperty(value = "")
    private Integer avgDuration;

    @ApiModelProperty(value = "")
    private Integer avgInterval;

    @ApiModelProperty(value = "")
    private Integer avgOffset;

    @ApiModelProperty(value = "")
    private Object virtualValue;

    @ApiModelProperty(value = "")
    private Object limitUp;

    @ApiModelProperty(value = "")
    private Object limitDown;

    @ApiModelProperty(value = "")
    private Boolean onlyRead;

    @ApiModelProperty(value = "")
    private Object tagValue;
}
package com.supcon.tptrecommend.feign.entity.autosupervision;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TagExcelImport {

    @ApiModelProperty(value = "工厂节点")
    private String plantNode;

    @ApiModelProperty(value = "工艺参数")
    private String evaluationItemName;

    @ApiModelProperty(value = "重要等级(非常重要：1;重要：2；一般：3;不填时，默认为重要）")
    private String importance;

    @ApiModelProperty(value = "前置判断条件(例如负荷位号>特定值)")
    private String prejudgeCondition;

    @ApiModelProperty(value = "位号")
    private String tagName;

    @ApiModelProperty(value = "位号类型")
    private String attribute;

    @ApiModelProperty(value = "位号描述")
    private String tagDesc;


    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "高高限")
    private String hh;


    @ApiModelProperty(value = "高限")
    private String ph;


    @ApiModelProperty(value = "低限")
    private String pl;


    @ApiModelProperty(value = "低低限")
    private String ll;

}
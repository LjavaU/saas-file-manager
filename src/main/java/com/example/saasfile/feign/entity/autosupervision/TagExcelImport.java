package com.example.saasfile.feign.entity.autosupervision;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TagExcelImport {

    @ApiModelProperty(value = "")
    private String plantNode;

    @ApiModelProperty(value = "")
    private String evaluationItemName;

    @ApiModelProperty(value = "")
    private String importance;

    @ApiModelProperty(value = "")
    private String prejudgeCondition;

    @ApiModelProperty(value = "")
    private String tagName;

    @ApiModelProperty(value = "")
    private String attribute;

    @ApiModelProperty(value = "")
    private String tagDesc;


    @ApiModelProperty(value = "")
    private String unit;

    @ApiModelProperty(value = "")
    private String hh;


    @ApiModelProperty(value = "")
    private String ph;


    @ApiModelProperty(value = "")
    private String pl;


    @ApiModelProperty(value = "")
    private String ll;

}
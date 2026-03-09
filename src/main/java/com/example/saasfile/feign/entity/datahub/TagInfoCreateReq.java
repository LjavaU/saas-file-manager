package com.example.saasfile.feign.entity.datahub;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(description = "")
public class TagInfoCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private String tagName;

    @ApiModelProperty(value = "")
    private String tagBaseName;

    @ApiModelProperty(value = "")
    private String tagDesc;

    @NotNull(message = "娴ｅ秴褰跨猾璇茬€烽敍姝縸")
    private Integer tagType = 4;

    @ApiModelProperty(value = "")
    private String unit;

    @ApiModelProperty(value = "")
    private Integer dataType;

    @ApiModelProperty(value = "")
    private Integer baseDataType;

    @ApiModelProperty(value = "")
    private Object limitUp;

    @ApiModelProperty(value = "")
    private Object limitDown;

    @ApiModelProperty(value = "")
    private Object limitUpUp;

    @ApiModelProperty(value = "")
    private Object limitDownDown;

    @ApiModelProperty(value = "")
    private Object limitUpUpUp;

    @ApiModelProperty(value = "")
    private Object limitDownDownDown;

    @ApiModelProperty(value = "")
    private Boolean onlyRead;
}
package com.supcon.tptrecommend.feign.entity.datahub;

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

    @ApiModelProperty(value = "系统位号名/位号名称")
    @NotNull(message = "系统位号名：{}")
    @Length(max = 255, message = "{系统位号名}：{}")
    private String tagName;

    @ApiModelProperty(value = "底层位号名")
    @Length(max = 255, message = "{底层位号名}：{}")
    private String tagBaseName;

    @ApiModelProperty(value = "位号描述")
    @Length(max = 255, message = "{位号描述}：{}")
    private String tagDesc;

    @NotNull(message = "位号类型：{}")
    private Integer tagType;

    @ApiModelProperty(value = "位号工程单位/位号单位")
    @Length(max = 20, message = "{位号工程单位}：{}")
    private String unit;

    //@ApiModelProperty(value = "位号数据类型")
    private Integer dataType = 10 ;

   // @ApiModelProperty(value = "底层位号数据类型")
    private Integer baseDataType;

   // @ApiModelProperty(value = "是否只读", example = "true")
    private Boolean onlyRead;
}
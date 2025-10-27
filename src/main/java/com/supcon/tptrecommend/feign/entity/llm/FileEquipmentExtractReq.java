package com.supcon.tptrecommend.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "文件设备信息提取请求实体")
public class FileEquipmentExtractReq {

    @ApiModelProperty(value = "文件类型")
    private String documentType;

    @ApiModelProperty(value = "markdown内容")
    private String markdownContent;


    @ApiModelProperty(value = "所属子类")
    private Integer subcategory;

}

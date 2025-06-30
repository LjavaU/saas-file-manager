package com.supcon.tptrecommend.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "文件分类返回体")
public class FileClassifyResp {

    @ApiModelProperty(value = "所属分类")
    private Integer category;

    @ApiModelProperty(value = "所属子类")
    private Integer subcategory;

    @ApiModelProperty(value = "摘要")
    private String summary;
}

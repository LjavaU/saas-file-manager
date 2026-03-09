package com.example.saasfile.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "")
public class FileClassifyResp {

    @ApiModelProperty(value = "")
    private Integer category;

    @ApiModelProperty(value = "")
    private Integer subcategory;

    @ApiModelProperty(value = "")
    private Integer third_level_category;

    @ApiModelProperty(value = "")
    private String summary;
}

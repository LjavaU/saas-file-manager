package com.example.saasfile.feign.entity.llm;

import cn.hutool.json.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "")
public class FileParseResp {
    @ApiModelProperty(value = "")
    private Integer category;

    @ApiModelProperty(value = "")
    private String summary;

    @ApiModelProperty(value = "")
    private JSONArray data;
}

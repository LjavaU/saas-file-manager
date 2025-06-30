package com.supcon.tptrecommend.feign.entity.llm;

import cn.hutool.json.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "文件解析返回内容")
public class FileParseResp {
    @ApiModelProperty(value = "所属分类")
    private String category;

    @ApiModelProperty(value = "内容概述")
    private String summary;

    @ApiModelProperty(value = "分析数据")
    private JSONArray data;
}

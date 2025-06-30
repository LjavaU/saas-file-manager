package com.supcon.tptrecommend.feign.entity.llm;

import cn.hutool.json.JSONArray;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(description = "文件内容提取返回体")
public class FileExtractResp {
    @ApiModelProperty(value = "分析数据")
    private JSONArray data;
}

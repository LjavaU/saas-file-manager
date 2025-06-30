package com.supcon.tptrecommend.feign.entity.llm;

import cn.hutool.json.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "文件实体映射返回体")
public class FileAlignmentResp {
    @ApiModelProperty(value = "文件表头和实体映射数据")
    private JSONObject data;
}

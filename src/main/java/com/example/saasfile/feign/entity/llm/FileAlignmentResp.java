package com.example.saasfile.feign.entity.llm;

import cn.hutool.json.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "")
public class FileAlignmentResp {
    @ApiModelProperty(value = "")
    private JSONObject data;
}

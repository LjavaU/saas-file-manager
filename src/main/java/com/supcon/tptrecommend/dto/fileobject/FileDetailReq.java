package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel(description = "获取文件详情请求体")
public class FileDetailReq {

    @ApiModelProperty(value = "文件全路径")
    @NotEmpty(message = "文件全路径不能为空")
    private List<String> paths;
}

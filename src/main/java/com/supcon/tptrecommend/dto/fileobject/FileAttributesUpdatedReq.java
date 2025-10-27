package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "文件属性更新请求体")
public class FileAttributesUpdatedReq {

    @ApiModelProperty(value = "文件全路径")
    @NotBlank(message = "文件全路径不能为空")
    private String objectName;

    @ApiModelProperty(value = "文件类别")
    private String category;

    @ApiModelProperty(value = "对应能力/应用")
    private String ability;
}

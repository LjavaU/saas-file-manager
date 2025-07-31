package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "文件属性更新请求体")
public class FileAttributesUpdatedReq {
    @ApiModelProperty(value = "文件Id")
    @NotNull(message = "文件Id不能为空")
    private Long fileId;

    @ApiModelProperty(value = "文件类别")
    private String category;

    @ApiModelProperty(value = "对应能力/应用")
    private String ability;
}

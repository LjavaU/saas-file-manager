package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel( description = "创建文件夹请求体")
public class CreateFolderReq {
    @ApiModelProperty(value = "文件夹名称")
    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;
}

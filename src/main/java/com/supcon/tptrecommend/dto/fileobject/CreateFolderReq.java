package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "创建文件夹请求体")
public class CreateFolderReq {
    @ApiModelProperty(value = "文件夹名称",example = "supcon")
    @NotBlank(message = "文件夹名称不能为空")
    @Length(max = 30, message = "文件夹名称不能超过30个字符")
    private String folderName;

    @ApiModelProperty(value = "文件夹类型 private：私有，tenant：租户共享 ", example = "private")
    private String folderType = "private";
}

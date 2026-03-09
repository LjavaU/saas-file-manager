package com.example.saasfile.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "")
public class CreateFolderReq {

    @ApiModelProperty(value = "")
    @NotBlank(message = "folderName is required")
    @Length(max = 30, message = "folderName is too long")
    private String folderName;

    @ApiModelProperty(value = "")
    private String folderType = "private";
}

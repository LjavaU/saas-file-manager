package com.example.saasfile.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "")
public class FileAttributesUpdatedReq {

    @ApiModelProperty(value = "")
    @NotBlank(message = "objectName is required")
    private String objectName;

    @ApiModelProperty(value = "")
    private String category;

    @ApiModelProperty(value = "")
    private String ability;
}

package com.example.saasfile.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(description = "")
public class SingleFileQueryReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    @NotBlank(message = "path cannot be blank")
    private String path;
}

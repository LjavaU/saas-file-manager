package com.example.saasfile.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel(description = "")
public class FileDetailReq {

    @ApiModelProperty(value = "")
    @NotEmpty(message = "paths cannot be empty")
    private List<String> paths;
}

package com.example.saasfile.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "")
public class FileStatisticsResp {
    @ApiModelProperty(value = "")
    private Long totalFiles;

    @ApiModelProperty(value = "")
    private Long totalSize;

}

package com.example.saasfile.dto.FileParse;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel(description = "")
@Builder
public class FileParseProgressResp {

    @ApiModelProperty(value = "")
    private Long fileId;

    @ApiModelProperty(value = "")
    private Integer parseProgress;

}

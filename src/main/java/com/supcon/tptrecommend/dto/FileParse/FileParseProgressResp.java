package com.supcon.tptrecommend.dto.FileParse;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel( description = "文件解析过程详情返回体")
@Builder
public class FileParseProgressResp {

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "解析进度")
    private Integer parseProgress;

}

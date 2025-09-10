package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "文件统计返回体")
public class FileStatisticsResp {
    @ApiModelProperty(value = "总文件数")
    private Long totalFiles;

    @ApiModelProperty(value = "总大小")
    private String totalSize;

}

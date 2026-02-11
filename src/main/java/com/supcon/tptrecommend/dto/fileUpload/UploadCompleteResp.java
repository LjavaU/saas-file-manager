package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "上传完成响应")
public class UploadCompleteResp {

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "对象路径")
    private String objectName;

    @ApiModelProperty(value = "对象ETag")
    private String etag;

    @ApiModelProperty(value = "是否触发了解析任务")
    private Boolean parseTriggered;

    @ApiModelProperty(value = "当前文件状态码")
    private Integer fileStatus;
}

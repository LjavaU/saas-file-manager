package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "分片上传完成请求")
public class MultipartUploadCompleteReq {

    @NotNull(message = "文件ID不能为空")
    @ApiModelProperty(value = "文件ID", required = true)
    private Long fileId;

    @NotBlank(message = "uploadId不能为空")
    @ApiModelProperty(value = "上传会话ID", required = true)
    private String uploadId;
}

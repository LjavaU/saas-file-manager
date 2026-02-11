package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "分片签名请求")
public class MultipartUploadSignReq {

    @NotNull(message = "文件ID不能为空")
    @ApiModelProperty(value = "文件ID", required = true)
    private Long fileId;

    @NotBlank(message = "uploadId不能为空")
    @ApiModelProperty(value = "上传会话ID", required = true)
    private String uploadId;

    @NotNull(message = "partNumber不能为空")
    @Min(value = 1, message = "partNumber不能小于1")
    @Max(value = 10000, message = "partNumber不能超过10000")
    @ApiModelProperty(value = "分片序号（从1开始）", required = true)
    private Integer partNumber;
}

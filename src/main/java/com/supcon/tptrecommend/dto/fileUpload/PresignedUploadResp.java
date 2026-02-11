package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "普通预签名上传初始化响应")
public class PresignedUploadResp {

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "桶名称")
    private String bucketName;

    @ApiModelProperty(value = "对象路径")
    private String objectName;

    @ApiModelProperty(value = "预签名上传URL")
    private String uploadUrl;

    @ApiModelProperty(value = "上传会话ID（回调时必传）")
    private String uploadId;

    @ApiModelProperty(value = "过期时间戳（毫秒）")
    private Long expireAt;
}

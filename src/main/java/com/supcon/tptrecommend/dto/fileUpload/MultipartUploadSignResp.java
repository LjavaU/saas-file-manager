package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "分片签名响应")
public class MultipartUploadSignResp {

    @ApiModelProperty(value = "分片序号")
    private Integer partNumber;

    @ApiModelProperty(value = "分片对象路径")
    private String partObjectName;

    @ApiModelProperty(value = "分片上传URL")
    private String uploadUrl;

    @ApiModelProperty(value = "过期时间戳（毫秒）")
    private Long expireAt;
}

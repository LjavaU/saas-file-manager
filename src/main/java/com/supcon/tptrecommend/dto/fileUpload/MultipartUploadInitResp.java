package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "分片上传初始化响应")
public class MultipartUploadInitResp {

    @ApiModelProperty(value = "文件ID")
    private Long fileId;

    @ApiModelProperty(value = "桶名称")
    private String bucketName;

    @ApiModelProperty(value = "最终对象路径")
    private String objectName;

    @ApiModelProperty(value = "上传会话ID")
    private String uploadId;

    @ApiModelProperty(value = "分片总数")
    private Integer totalParts;

    @ApiModelProperty(value = "过期时间戳（毫秒）")
    private Long expireAt;
}

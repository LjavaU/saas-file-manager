package com.supcon.tptrecommend.dto.fileshare;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "文件分享请求体")
public class FileShareRequest {

    @NotBlank(message = "桶名不能为空")
    @ApiModelProperty(value = "桶名",required = true)
    private String bucketName;

    @NotBlank(message = "文件全路径不能为空")
    @ApiModelProperty(value = "文件路径",required = true)
    private String objectName;

    @ApiModelProperty("过期时间（秒），默认1天")
    private long expirationSecond = 86400;
}

package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "上传回调请求")
public class UploadCallbackReq {

    @NotNull(message = "文件ID不能为空")
    @ApiModelProperty(value = "文件ID", required = true)
    private Long fileId;

    @NotBlank(message = "uploadId不能为空")
    @ApiModelProperty(value = "上传会话ID", required = true)
    private String uploadId;

    @Pattern(regexp = "^$|^[a-fA-F0-9]{32}$", message = "MD5必须是32位十六进制")
    @ApiModelProperty(value = "文件MD5（可选）")
    private String fileMd5;

    @ApiModelProperty(value = "客户端拿到的ETag（可选）")
    private String etag;
}

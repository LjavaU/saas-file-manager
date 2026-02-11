package com.supcon.tptrecommend.dto.fileUpload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@ApiModel(description = "普通预签名上传初始化请求")
public class PresignedUploadInitReq {

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255")
    @ApiModelProperty(value = "原始文件名", required = true)
    private String originalName;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    @ApiModelProperty(value = "文件大小（字节）", required = true)
    private Long fileSize;

    @Size(max = 128, message = "contentType长度不能超过128")
    @ApiModelProperty(value = "MIME类型", example = "application/pdf")
    private String contentType;

    @Size(max = 256, message = "路径长度不能超过256")
    @ApiModelProperty(value = "上传目录（相对路径）", example = "report/2026")
    private String path;

    @Pattern(regexp = "^$|^[a-fA-F0-9]{32}$", message = "MD5必须是32位十六进制")
    @ApiModelProperty(value = "文件MD5（可选）", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileMd5;
}

package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * MinIO 文件元数据表
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Data
@Builder
@ApiModel(value = "MinIO 文件元数据表-数据创建模型", description = "MinIO 文件元数据表")
public class FileObjectCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户 ID(最大64字符)", example = "用户 ID")
    private Long userId;

    @ApiModelProperty(value = "用户 名称(最大64字符)", example = "用户 名称")
    private String userName;

    @ApiModelProperty(value = "MinIO 中对象的 key（路径）", example = "MinIO 中对象的 key（路径）")
    private String objectName;

    @ApiModelProperty(value = "原始文件名", example = "原始文件名")
    private String originalName;

    @ApiModelProperty(value = "桶名称(最大64字符)", example = "桶名称")
    private String bucketName;

    @ApiModelProperty(value = "MIME 文件类型(最大128字符)", example = "MIME 文件类型")
    private String contentType;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty(value = "标签", example = "标签")
    private String tags;

}
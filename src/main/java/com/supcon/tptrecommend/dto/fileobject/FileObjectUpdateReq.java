package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
@ApiModel(value = "MinIO 文件元数据表-数据更新模型", description = "MinIO 文件元数据表")
public class FileObjectUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{数据ID}：{}")
    @ApiModelProperty(value = "数据ID", example = "0", required = true)
    private Long id;

    @ApiModelProperty(value = "用户 ID(最大64字符)", example = "用户 ID")
    private String userId;

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
package com.supcon.tptrecommend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supcon.system.base.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(callSuper = true)
@TableName("file_object")
@ApiModel(value = "MinIO 文件元数据表-数据库基础实体", description = "MinIO 文件元数据表")
public class FileObject extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户 ID", example = "用户 ID")
    private String userId;

    @ApiModelProperty(value = "租户 ID", example = "租户 ID")
    private String tenantId;

    @ApiModelProperty(value = "用户 名称", example = "用户 名称")
    private String userName;

    @ApiModelProperty(value = "MinIO 中对象的 key（路径）", example = "MinIO 中对象的 key（路径）")
    private String objectName;

    @ApiModelProperty(value = "原始文件名", example = "原始文件名")
    private String originalName;

    @ApiModelProperty(value = "桶名称", example = "桶名称")
    private String bucketName;

    @ApiModelProperty(value = "MIME 文件类型", example = "MIME 文件类型")
    private String contentType;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty(value = "标签", example = "标签")
    private String tags;

}
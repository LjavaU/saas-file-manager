package com.supcon.tptrecommend.dto.fileobject;

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
@ApiModel(value = "MinIO 文件元数据表-数据响应模型", description = "MinIO 文件元数据表")
public class FileObjectResp extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "租户 ID")
    private String tenantId;

    @ApiModelProperty(value = "用户 ID", example = "用户 ID")
    private Long userId;

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

    @ApiModelProperty(value = "文件大小")
    private String fileSize;

    @ApiModelProperty(value = "所属分类")
    private String category;


    @ApiModelProperty(value = "对应能力&应用")
    private String ability;


    @ApiModelProperty(value = "内容概述")
    private String contentOverview;

    @ApiModelProperty(value = "文件解析状态，【0-未解析，1-解析完成，2-解释失败】")
    private Integer fileStatus;

    @ApiModelProperty(value = "所属分类标识")
    private String categoryIdentifier;
}
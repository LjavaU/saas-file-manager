package com.supcon.tptrecommend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supcon.system.base.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 文件推荐问题生成
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-08-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
    @TableName("file_recommendation")
@ApiModel(value = "文件推荐问题生成-数据库基础实体", description = "文件推荐问题生成")
public class FileRecommendation extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "租户 ID", example = "租户 ID", required = true)
    private String tenantId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "关键词", example = "关键词")
    private String keyword;

    @ApiModelProperty(value = "问题集", example = "问题集")
    private String questions;

}
package com.supcon.tptrecommend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.supcon.system.base.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 问题主表
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("question")
@ApiModel(value = "问题主表-数据库基础实体", description = "问题主表")
public class Question extends BasicEntity<Long>  implements Serializable {

    private static final long serialVersionUID = 1L;



    @ApiModelProperty(value = "租户id", example = "租户id", required = true)
    @TableField(fill = FieldFill.INSERT)
    private String tenantId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "行业", example = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位", example = "岗位")
    private String post;

    @ApiModelProperty(value = "装置", example = "装置")
    private String device;

    @ApiModelProperty(value = "问题", example = "问题")
    private String content;



}
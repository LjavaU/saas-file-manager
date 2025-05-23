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
 * 用户信息表
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_info")
@ApiModel(value = "用户信息表-数据库基础实体", description = "用户信息表")
public class UserInfo extends BasicEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "租户id", example = "租户id", required = true)
    @TableField(fill = FieldFill.INSERT)
    private String tenantId;


    @ApiModelProperty(value = "用户id", example = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户名", example = "用户名")
    private String userName;

    @ApiModelProperty(value = "行业", example = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位", example = "岗位")
    private String post;

    @ApiModelProperty(value = "装置", example = "装置")
    private String device;



}
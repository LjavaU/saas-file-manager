package com.supcon.tptrecommend.dto.userinfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@EqualsAndHashCode
@ApiModel(value = "用户信息表-数据响应模型", description = "用户信息表")
public class UserInfoResp implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一字段", required = true)
    private Long id;

    @ApiModelProperty(value = "租户id", example = "租户id", required = true)
    private String tenantId;

    @ApiModelProperty(value = "用户名", example = "用户名")
    private String userName;

    @ApiModelProperty(value = "行业", example = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位", example = "岗位")
    private String post;

    @ApiModelProperty(value = "装置", example = "装置")
    private String device;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

}
package com.supcon.tptrecommend.dto.userinfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
@ApiModel(value = "用户信息表-数据更新模型", description = "用户信息表")
public class UserInfoUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一字段")
    @NotNull(message = "{唯一字段}：{}")
    private Long id;

    @ApiModelProperty(value = "用户名(最大50字符)", example = "用户名")
    private String userName;

    @ApiModelProperty(value = "行业(最大100字符)", example = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位(最大50字符)", example = "岗位")
    private String post;

    @ApiModelProperty(value = "装置(最大50字符)", example = "装置")
    private String device;

}
package com.supcon.tptrecommend.dto.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

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
@EqualsAndHashCode
@ApiModel(value = "问题主表-数据响应模型", description = "问题主表")
public class QuestionResp implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一字段", required = true)
    private Long id;

    @ApiModelProperty(value = "租户id", example = "租户id", required = true)
    private String tenantId;

    @ApiModelProperty(value = "行业", example = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位", example = "岗位")
    private String post;

    @ApiModelProperty(value = "装置", example = "装置")
    private String device;

    @ApiModelProperty(value = "问题", example = "问题")
    private String content;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

}
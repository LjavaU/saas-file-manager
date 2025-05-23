package com.supcon.tptrecommend.dto.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
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
@ApiModel(value = "问题主表-数据更新模型", description = "问题主表")
public class QuestionUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "唯一字段")
    @NotNull(message = "{唯一字段}：{}")
    private Long id;

    @ApiModelProperty(value = "行业(最大100字符)", example = "行业")
    private String industry;

    @ApiModelProperty(value = "岗位(最大50字符)", example = "岗位")
    private String post;

    @ApiModelProperty(value = "装置(最大50字符)", example = "装置")
    private String device;

    @ApiModelProperty(value = "问题(最大255字符)", example = "问题")
    private String content;

}
package com.example.saasfile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.saasfile.support.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
    @TableName("file_recommendation")
@ApiModel(description = "")
public class FileRecommendation extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "")
    private String tenantId;

    @ApiModelProperty(value = "")
    private Long fileId;

    @ApiModelProperty(value = "")
    private String keyword;

    @ApiModelProperty(value = "")
    private String questions;

}
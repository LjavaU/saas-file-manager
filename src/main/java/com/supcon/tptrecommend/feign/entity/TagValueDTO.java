package com.supcon.tptrecommend.feign.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.supcon.systemcommon.valid.ValidInsert;
import com.supcon.systemcommon.valid.ValidUpdate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@ApiModel(value = "数据模型", description = "数据模型")
public class TagValueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JSONField(name="ds_id")
    @ApiModelProperty(value = "数据源id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JSONField(name="ds_id")
    @ApiModelProperty(value = "数据源id")
    private Long dsId;

    @ApiModelProperty(value = "位号名", required = true)
    @JSONField(name="tag_name")
    @NotBlank(groups = {ValidInsert.class, ValidUpdate.class}, message = "{system-client.exception.tagNameNonNull}")
    private String tagName;

    @ApiModelProperty(value = "位号值", required = true)
    @JSONField(name="tag_value")
    private Object tagValue;

    @ApiModelProperty(value = "实时数据库tag返回时间", required = true)
    @JSONField(name="tag_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(groups = {ValidInsert.class, ValidUpdate.class}, message = "{system-client.exception.tagTimeNonNull}")
    private LocalDateTime tagTime;

    @ApiModelProperty(value = "查询实时数据库时间", required = true)
    @JSONField(name="app_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appTime;

    @ApiModelProperty(value = "质量码")
    private Long quality;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "是否成功")
    private Boolean isSuccess;

    @ApiModelProperty(value = "消息")
    private String message;

    public TagValueDTO() {
    }

    public TagValueDTO(Long id, Boolean isSuccess, String message) {
        this.setId(id);
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public TagValueDTO(String tagName, Boolean isSuccess, String message) {
        this.setTagName(tagName);
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public TagValueDTO(String tagName, Long dsId) {
        this.setTagName(tagName);
        this.setDsId(dsId);
        this.setCreateTime(LocalDateTime.now());
        this.setTagTime(LocalDateTime.now());
        this.setAppTime(LocalDateTime.now());
        this.isSuccess = true;
    }
}
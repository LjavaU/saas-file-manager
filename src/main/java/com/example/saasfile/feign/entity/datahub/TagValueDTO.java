package com.example.saasfile.feign.entity.datahub;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.saasfile.support.validation.ValidInsert;
import com.example.saasfile.support.validation.ValidUpdate;
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
@ApiModel(description = "")
public class TagValueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @ApiModelProperty(value = "")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @JsonProperty("ds_id")
    @ApiModelProperty(value = "")
    private Long dsId;

    @ApiModelProperty(value = "")
    @JsonProperty("tag_name")
    @NotBlank(groups = {ValidInsert.class, ValidUpdate.class}, message = "{system-client.exception.tagNameNonNull}")
    private String tagName;

    @ApiModelProperty(value = "")
    @JsonProperty("tag_value")
    private Object tagValue;

    @ApiModelProperty(value = "")
    @JsonProperty("tag_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(groups = {ValidInsert.class, ValidUpdate.class}, message = "{system-client.exception.tagTimeNonNull}")
    private LocalDateTime tagTime;

    @ApiModelProperty(value = "")
    @JsonProperty("app_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appTime;

    @ApiModelProperty(value = "")
    private Long quality;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "")
    private Boolean isSuccess;

    @ApiModelProperty(value = "")
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

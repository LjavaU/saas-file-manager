package com.supcon.tptrecommend.feign.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value = "组分-数据创建模型", description = "组分")
public class TmpLabelComponentCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{}：{}")
    private Integer compId;

    @NotBlank(message = "{}：{}")
    @Length(max = 100, message = "{}：{}")
    private String compName;

    @NotNull(message = "{}：{}")
    private Float compRatio;

    private String compDesc;

}
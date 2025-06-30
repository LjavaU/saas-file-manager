package com.supcon.tptrecommend.feign.entity.tmplabel;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(value = "-数据创建模型", description = "")
public class TmpLabelTargetCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{}：{}")
    private Integer targetId;

    @NotBlank(message = "{}：{}")
    @Length(max = 100, message = "{}：{}")
    private String targetName;

    @NotNull(message = "{}：{}")
    private Integer targetType;

    private String targetDesc;

}
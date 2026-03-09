package com.example.saasfile.feign.entity.tmplabel;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(description = "")
public class TmpLabelTargetCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{}閿涙}")
    private Integer targetId;

    @NotBlank(message = "{}閿涙}")
    @Length(max = 100, message = "{}閿涙}")
    private String targetName;

    @NotNull(message = "{}閿涙}")
    private Integer targetType;

    private String targetDesc;

}
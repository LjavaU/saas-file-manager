package com.example.saasfile.feign.entity.tmplabel;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel(description = "")
public class TmpLabelComponentCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{}閿涙}")
    private Integer compId;

    @NotBlank(message = "{}閿涙}")
    @Length(max = 100, message = "{}閿涙}")
    private String compName;

    @NotNull(message = "{}閿涙}")
    private Float compRatio;

    private String compDesc;

}
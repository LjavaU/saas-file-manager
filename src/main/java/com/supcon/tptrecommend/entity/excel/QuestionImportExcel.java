package com.supcon.tptrecommend.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.supcon.systemcomponent.excel.model.ExcelErrorMessage;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class QuestionImportExcel implements ExcelErrorMessage {



    @ExcelProperty(value = "租户id", index = 0)
    private String tenantId;

    @ExcelProperty(value = "行业", index = 1)
    @NotBlank
    private String industry;

    @ExcelProperty(value = "岗位", index = 2)
    @NotBlank
    private String post;

    @ExcelProperty(value = "装置", index = 3)
    @NotBlank
    private String device;

    @ExcelProperty(value = "问题", index = 4)
    @NotBlank
    private String content;

    @ExcelProperty(value = "错误信息", index = 5)
    private String errorMsg;
}
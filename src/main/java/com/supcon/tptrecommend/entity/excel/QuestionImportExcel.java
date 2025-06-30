package com.supcon.tptrecommend.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.supcon.systemcomponent.excel.model.ExcelErrorMessage;
import com.supcon.tptrecommend.integration.excel.YesNoToIntegerConverter;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class QuestionImportExcel implements ExcelErrorMessage {


    @ExcelProperty(value = "行业", index = 0)
    private String industry;

    @ExcelProperty(value = "岗位", index = 1)
    private String post;

    @ExcelProperty(value = "装置", index = 2)
    private String device;

    @ExcelProperty(value = "问题", index = 3)
    @NotBlank(message = "问题不能为空")
    private String content;

    @ExcelProperty(value = "是否是共性问题", index = 4, converter = YesNoToIntegerConverter.class)
    private Integer isCommon;

    @ExcelProperty(value = "错误信息", index = 5)
    private String errorMsg;

}
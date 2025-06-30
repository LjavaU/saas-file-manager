package com.supcon.tptrecommend.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "文件实体映射请求体")
public class FileAlignmentReq {

    @ApiModelProperty(value = "文档类型")
    private String documentType ;

    @ApiModelProperty(value = "数据库表实体结构体")
    private String databaseSchema;


    @ApiModelProperty(value = "用户上传文件表头")
    private String excelHeader;

    @ApiModelProperty(value = "业务分类")
    private Integer subcategory ;
}

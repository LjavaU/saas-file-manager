package com.example.saasfile.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class FileAlignmentReq {

    @ApiModelProperty(value = "")
    private String documentType ;

    @ApiModelProperty(value = "")
    private String databaseSchema;


    @ApiModelProperty(value = "")
    private String excelHeader;

    @ApiModelProperty(value = "")
    private Integer subcategory ;
}

package com.example.saasfile.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class FileEquipmentExtractReq {

    @ApiModelProperty(value = "")
    private String documentType;

    @ApiModelProperty(value = "")
    private String markdownContent;


    @ApiModelProperty(value = "")
    private Integer subcategory;

}

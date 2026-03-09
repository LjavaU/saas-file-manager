package com.example.saasfile.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class FileExtractReq {

    @ApiModelProperty(value = "")
    private String markdownContent;

    @ApiModelProperty(value = "")
    private String previousMarkdownContent;

    @ApiModelProperty(value = "")
    private String documentType;

    @ApiModelProperty(value = "")
    private Integer subcategory;
}

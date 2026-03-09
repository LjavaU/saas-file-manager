package com.example.saasfile.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class FileParseReq {

    @ApiModelProperty(value = "")
    private String markdownContent;

    @ApiModelProperty(value = "")
    private String headMarkdownContent;

    @ApiModelProperty(value = "")
    private String previousMarkdownContent;

    @ApiModelProperty(value = "")
    private String documentType;
}

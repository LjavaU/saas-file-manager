package com.example.saasfile.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "")
public class FileClassifyReq {

    @ApiModelProperty(value = "")
    private String headMarkdownContent;

    @ApiModelProperty(value = "")
    private String documentType;
}

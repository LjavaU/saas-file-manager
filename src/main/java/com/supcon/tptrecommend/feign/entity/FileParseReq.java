package com.supcon.tptrecommend.feign.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "markdown文件解析请求体")
public class FileParseReq {

    @ApiModelProperty(value = "markdown文件内容")
    private String markdownContent;

    @ApiModelProperty(value = "markdown头标题内容")
    private String headMarkdownContent;

    @ApiModelProperty(value = "前段markdown内容")
    private String previousMarkdownContent;

    @ApiModelProperty(value = "文件类型")
    private String documentType;
}

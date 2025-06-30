package com.supcon.tptrecommend.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "文件内容提取请求体")
public class FileExtractReq {

    @ApiModelProperty(value = "markdown文件内容")
    private String markdownContent;

    @ApiModelProperty(value = "前段markdown内容")
    private String previousMarkdownContent;

    @ApiModelProperty(value = "文件类型")
    private String documentType;

    @ApiModelProperty(value = "所属子类")
    private Integer subcategory;
}

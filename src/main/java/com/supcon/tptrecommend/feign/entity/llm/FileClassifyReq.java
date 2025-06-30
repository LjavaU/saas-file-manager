package com.supcon.tptrecommend.feign.entity.llm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "文件分类请求体")
public class FileClassifyReq {

    @ApiModelProperty(value = "markdown头标题内容")
    private String headMarkdownContent;

    @ApiModelProperty(value = "文件类型")
    private String documentType;
}

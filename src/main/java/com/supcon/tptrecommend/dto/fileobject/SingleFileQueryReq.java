package com.supcon.tptrecommend.dto.fileobject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel( description = "获取minio单个文件请求体")
public class SingleFileQueryReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件路径(minio对象中的key)")
    private String path;
}

package com.example.saasfile.feign.entity.pid;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "")
public class DcsLoopMetadata {

    @ApiModelProperty(value = "")
    private Long datasourceId = 1L;

    @ApiModelProperty(value = "")
    private Long dcsEquipmentId = 1L;

    @ApiModelProperty(value = "")
    private String descript;

    @ApiModelProperty(value = "")
    private Long groupId =2L;

    @ApiModelProperty(value = "")
    private String groupNamePath;

    @ApiModelProperty(value = "")
    private String loopName;

    @ApiModelProperty(value = "")
    private Integer loopProperty;

    @ApiModelProperty(value = "")
    private Integer loopType;

    @ApiModelProperty(value = "")
    private Integer samplingTime;

    @ApiModelProperty(value = "")
    private Integer steadyStateTime;

    @ApiModelProperty(value = "")
    private String functionBlockType;


}

package com.supcon.tptrecommend.feign.entity.pid;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "DCS回路元数据实体")
public class DcsLoopMetadata {

    @ApiModelProperty(value = "数据源ID")
    private Long datasourceId = 1L;

    @ApiModelProperty(value = "DCS设备ID")
    private Long dcsEquipmentId = 1L;

    @ApiModelProperty(value = "描述信息")
    private String descript;

    @ApiModelProperty(value = "组ID")
    private Long groupId =2L;

    @ApiModelProperty(value = "组名称路径")
    private String groupNamePath;

    @ApiModelProperty(value = "回路名称/名称")
    private String loopName;

    @ApiModelProperty(value = "回路属性")
    private Integer loopProperty;

    @ApiModelProperty(value = "回路类型")
    private Integer loopType;

    @ApiModelProperty(value = "采样时间")
    private Integer samplingTime;

    @ApiModelProperty(value = "稳态时间")
    private Integer steadyStateTime;

    @ApiModelProperty(value = "功能块类型")
    private String functionBlockType;


}

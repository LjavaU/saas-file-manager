package com.supcon.tptrecommend.feign.entity.tmplabel;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Data
@ApiModel(value = "装置-数据创建模型", description = "装置")
public class TmpLabelDeviceCreateReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "{}：{}")
    private Integer deviceId;

    private String deviceTag;

    @NotBlank(message = "{}：{}")
    @Length(max = 100, message = "{}：{}")
    private String deviceName;

    private String deviceDesc;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TmpLabelDeviceCreateReq that = (TmpLabelDeviceCreateReq) o;
        return Objects.equals(deviceTag, that.deviceTag) && Objects.equals(deviceName, that.deviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceTag, deviceName);
    }
}
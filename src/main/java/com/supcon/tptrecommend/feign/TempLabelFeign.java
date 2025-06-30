package com.supcon.tptrecommend.feign;

import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.feign.entity.tmplabel.TmpLabelComponentCreateReq;
import com.supcon.tptrecommend.feign.entity.tmplabel.TmpLabelDeviceCreateReq;
import com.supcon.tptrecommend.feign.entity.tmplabel.TmpLabelTargetCreateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "tempLabel", url = "${service.tempLabel.address:templabel地址不能为空}", fallbackFactory = TempLabelFeign.TempLabelFeignFallBack.class)
public interface TempLabelFeign {

    @PostMapping("/api/tmp-label-device/batchSave")
    SupResult<Boolean> addDevice(@Valid @RequestBody SupRequestBody<List<TmpLabelDeviceCreateReq>> body);

    @PostMapping("/api/tmp-label-component/batchSave")
    SupResult<Boolean> addComponent(@Valid @RequestBody SupRequestBody<List<TmpLabelComponentCreateReq>> body);

    @PostMapping("/api/tmp-label-target/batchSave")
    SupResult<Boolean> addItem(@Valid @RequestBody SupRequestBody<List<TmpLabelTargetCreateReq>> body);

    @Slf4j
    @Component
    class TempLabelFeignFallBack implements FallbackFactory<TempLabelFeign> {

        @Override
        public TempLabelFeign create(Throwable cause) {
            log.error("tempLabel调用出错", cause);
            return new TempLabelFeign() {
                @Override
                public SupResult<Boolean> addDevice(SupRequestBody<List<TmpLabelDeviceCreateReq>> body) {
                    log.error("/api/tmp-label-device/batchSave接口访问出错");
                    return SupResult.error("/api/tmp-label-device/batchSave接口调用异常!");
                }

                @Override
                public SupResult<Boolean> addComponent(SupRequestBody<List<TmpLabelComponentCreateReq>> body) {
                    log.error("/api/tmp-label-component/batchSave接口访问出错");
                    return SupResult.error("/api/tmp-label-component/batchSave接口调用异常!");
                }

                @Override
                public SupResult<Boolean> addItem(SupRequestBody<List<TmpLabelTargetCreateReq>> body) {
                    log.error("//api/tmp-label-target/batchSave接口访问出错");
                    return SupResult.error("/api/tmp-label-target/batchSave接口调用异常!");
                }
            };
        }
    }
}

package com.example.saasfile.feign;

import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.feign.entity.tmplabel.TmpLabelComponentCreateReq;
import com.example.saasfile.feign.entity.tmplabel.TmpLabelDeviceCreateReq;
import com.example.saasfile.feign.entity.tmplabel.TmpLabelTargetCreateReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "tempLabel", url = "${service.tempLabel.address:http://localhost}", fallbackFactory = TempLabelFeign.TempLabelFeignFallBack.class)
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
            log.error("TempLabel service call failed", cause);
            return new TempLabelFeign() {
                @Override
                public SupResult<Boolean> addDevice(SupRequestBody<List<TmpLabelDeviceCreateReq>> body) {
                    log.error("/api/tmp-label-device/batchSave failed");
                    return SupResult.error("TempLabel addDevice failed");
                }

                @Override
                public SupResult<Boolean> addComponent(SupRequestBody<List<TmpLabelComponentCreateReq>> body) {
                    log.error("/api/tmp-label-component/batchSave failed");
                    return SupResult.error("TempLabel addComponent failed");
                }

                @Override
                public SupResult<Boolean> addItem(SupRequestBody<List<TmpLabelTargetCreateReq>> body) {
                    log.error("/api/tmp-label-target/batchSave failed");
                    return SupResult.error("TempLabel addItem failed");
                }
            };
        }
    }
}

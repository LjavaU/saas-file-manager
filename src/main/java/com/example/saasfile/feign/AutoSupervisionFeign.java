package com.example.saasfile.feign;

import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.feign.entity.autosupervision.ControlExcelExport;
import com.example.saasfile.feign.entity.autosupervision.TagExcelImport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "autoSupervision", url = "${service.autoSupervision.address:http://localhost}", fallbackFactory = AutoSupervisionFeign.AutoSupervisionFeignFallBack.class)
public interface AutoSupervisionFeign {

    @PostMapping("/api/outer/evaluationItemImport")
    SupResult<List<ControlExcelExport>> importBottomTagExcelOrigin(@RequestBody SupRequestBody<List<TagExcelImport>> body,
                                                                   @RequestParam("groupType") Integer groupType);

    @Slf4j
    @Component
    class AutoSupervisionFeignFallBack implements FallbackFactory<AutoSupervisionFeign> {

        @Override
        public AutoSupervisionFeign create(Throwable cause) {
            log.error("AutoSupervision service call failed", cause);
            return new AutoSupervisionFeign() {
                @Override
                public SupResult<List<ControlExcelExport>> importBottomTagExcelOrigin(SupRequestBody<List<TagExcelImport>> body, Integer groupType) {
                    log.error("/api/outer/evaluationItemImport failed");
                    return SupResult.error("AutoSupervision import failed");
                }
            };
        }
    }
}

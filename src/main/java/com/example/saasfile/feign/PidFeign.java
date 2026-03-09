package com.example.saasfile.feign;

import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.feign.entity.pid.DcsLoopMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PidFeign", url = "${service.pid.address:http://localhost}", fallbackFactory = PidFeign.PidFeignFallBack.class)
public interface PidFeign {

    @PostMapping("/api/inter-api/pid-configuration/v1/loop/add")
    SupResult<Object> addLoop(@RequestBody DcsLoopMetadata dcsLoopMetadata);

    @Slf4j
    @Component
    class PidFeignFallBack implements FallbackFactory<PidFeign> {
        @Override
        public PidFeign create(Throwable cause) {
            return new PidFeign() {
                @Override
                public SupResult<Object> addLoop(DcsLoopMetadata dcsLoopMetadata) {
                    log.error("Call PidFeign#addLoop failed", cause);
                    return SupResult.error("PID loop create failed");
                }
            };
        }
    }
}

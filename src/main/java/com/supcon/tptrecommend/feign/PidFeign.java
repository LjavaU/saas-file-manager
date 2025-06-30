package com.supcon.tptrecommend.feign;

import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.feign.entity.pid.DcsLoopMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PidFeign", url = "${service.pid.address:pid服务地址不能为空}", fallbackFactory = PidFeign.PidFeignFallBack.class)
public interface PidFeign {

    /**
     * 新增回路数据
     *
     * @param dcsLoopMetadata DCS 循环元数据
     * @return {@link SupResult }<{@link String }>
     * @author luhao
     * @since 2025/06/27 13:49:36
     */
    @PostMapping("/api/inter-api/pid-configuration/v1/loop/add")
    SupResult<Object> addLoop(@RequestBody DcsLoopMetadata dcsLoopMetadata);

    @Slf4j
    @Component
    class PidFeignFallBack implements FallbackFactory<PidFeign> {
        @Override
        public PidFeign create(Throwable cause) {
            log.error("PID服务调用出错", cause);
            return new PidFeign() {
                @Override
                public SupResult<Object> addLoop(DcsLoopMetadata dcsLoopMetadata) {
                    log.error("inter-api/pid-configuration/v1/loop/add接口访问出错");
                    return SupResult.error("inter-api/pid-configuration/v1/loop/add接口调用异常!");
                }
            };
        }
    }
}

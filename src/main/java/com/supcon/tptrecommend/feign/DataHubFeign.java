package com.supcon.tptrecommend.feign;

import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.feign.entity.TagInfoCreateReq;
import com.supcon.tptrecommend.feign.entity.TagInfoResp;
import com.supcon.tptrecommend.feign.entity.TagValueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "dataHub", url = "${service.dataHub.address:数据中心地址不能为空}", fallbackFactory = DataHubFeign.DataHubFeignFallBack.class)
public interface DataHubFeign {

    @PostMapping("/api/tag-info/batchAdd")
     SupResult<List<TagInfoResp>> batchAdd(@Valid @RequestBody SupRequestBody<List<TagInfoCreateReq>> body);

    @PostMapping("/api/tag-value/importTagValue")
    SupResult<Boolean> importTagValue(@RequestBody @Valid SupRequestBody<List<TagValueDTO>> body);
    @Slf4j
    @Component
    class DataHubFeignFallBack implements FallbackFactory<DataHubFeign> {

        @Override
        public DataHubFeign create(Throwable cause) {
            log.error("模型调用出错", cause);
            return new DataHubFeign() {
                @Override
                public  SupResult<List<TagInfoResp>> batchAdd(@Valid @RequestBody SupRequestBody<List<TagInfoCreateReq>> body) {
                    log.error("/api/tag-info/add接口访问出错");
                    return SupResult.error("/api/tag-info/add接口调用异常!");
                }

                @Override
                public SupResult<Boolean> importTagValue(SupRequestBody<List<TagValueDTO>> body) {
                    return SupResult.error("/api/tag-value/importTagValue接口调用异常!");
                }
            };

        }
    }
}

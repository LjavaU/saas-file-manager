package com.example.saasfile.feign;

import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.feign.entity.datahub.TagInfoCreateReq;
import com.example.saasfile.feign.entity.datahub.TagInfoResp;
import com.example.saasfile.feign.entity.datahub.TagValueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@FeignClient(name = "dataHub", url = "${service.dataHub.address:http://localhost}", fallbackFactory = DataHubFeign.DataHubFeignFallBack.class)
public interface DataHubFeign {

    @PostMapping("/api/tag-info/batchAdd")
    SupResult<List<TagInfoResp>> batchAdd(@Valid @RequestBody SupRequestBody<List<TagInfoCreateReq>> body);

    @PostMapping("/api/tag-value/importTagValue")
    SupResult<Map<String, Collection<String>>> importTagValue(@RequestBody @Valid SupRequestBody<List<TagValueDTO>> body);

    @Slf4j
    @Component
    class DataHubFeignFallBack implements FallbackFactory<DataHubFeign> {

        @Override
        public DataHubFeign create(Throwable cause) {
            return new DataHubFeign() {
                @Override
                public SupResult<List<TagInfoResp>> batchAdd(@Valid @RequestBody SupRequestBody<List<TagInfoCreateReq>> body) {
                    log.error("Call DataHubFeign#batchAdd failed", cause);
                    return SupResult.error("DataHub batchAdd failed");
                }

                @Override
                public SupResult<Map<String, Collection<String>>> importTagValue(SupRequestBody<List<TagValueDTO>> body) {
                    log.error("Call DataHubFeign#importTagValue failed", cause);
                    return SupResult.error("DataHub importTagValue failed");
                }
            };
        }
    }
}

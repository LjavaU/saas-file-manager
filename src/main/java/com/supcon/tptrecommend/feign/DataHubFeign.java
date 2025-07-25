package com.supcon.tptrecommend.feign;

import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.feign.entity.datahub.TagInfoCreateReq;
import com.supcon.tptrecommend.feign.entity.datahub.TagInfoResp;
import com.supcon.tptrecommend.feign.entity.datahub.TagValueDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * 数据中心服务
 *
 * @author luhao
 * @since 2025/06/25 18:38:19
 */
@FeignClient(name = "dataHub", url = "${service.dataHub.address:数据中心地址不能为空}", fallbackFactory = DataHubFeign.DataHubFeignFallBack.class)
public interface DataHubFeign {

    /**
     * 批量添加位号信息
     *
     * @param body 请求体
     * @return {@link SupResult }<{@link List }<{@link TagInfoResp }>>
     * @author luhao
     * @since 2025/06/25 18:38:16
     */
    @PostMapping("/api/tag-info/batchAdd")
    SupResult<List<TagInfoResp>> batchAdd(@Valid @RequestBody SupRequestBody<List<TagInfoCreateReq>> body);

    /**
     * 批量添加位号历史数据
     *
     * @param body 请求体
     * @return {@link SupResult }<{@link Boolean }>
     * @author luhao
     * @since 2025/06/25 18:38:49
     */
    @PostMapping("/api/tag-value/importTagValue")
    SupResult<Boolean> importTagValue(@RequestBody @Valid SupRequestBody<List<TagValueDTO>> body);

    @Slf4j
    @Component
    class DataHubFeignFallBack implements FallbackFactory<DataHubFeign> {

        @Override
        public DataHubFeign create(Throwable cause) {
            return new DataHubFeign() {
                @Override
                public SupResult<List<TagInfoResp>> batchAdd(@Valid @RequestBody SupRequestBody<List<TagInfoCreateReq>> body) {
                    log.error("调用 DataHubFeign#batchAdd 失败", cause);
                    return SupResult.error("/api/tag-info/add接口调用异常!");
                }

                @Override
                public SupResult<Boolean> importTagValue(SupRequestBody<List<TagValueDTO>> body) {
                    log.error("调用 DataHubFeign#importTagValue 失败", cause);
                    return SupResult.error("/api/tag-value/importTagValue接口调用异常!");
                }
            };

        }
    }
}

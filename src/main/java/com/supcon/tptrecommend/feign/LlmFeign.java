package com.supcon.tptrecommend.feign;

import com.supcon.tptrecommend.feign.entity.FileParseReq;
import com.supcon.tptrecommend.feign.entity.FileParseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * LLM  调用
 *
 * @author luhao
 * @date 2025/06/03 20:10:02
 */
@FeignClient(name = "llm", url = "${service.llm.address:大模型地址不能为空}", fallbackFactory = LlmFeign.LlmFeignFallBack.class)
public interface LlmFeign {

    @PostMapping(value = "/api/file/parsing")
    FileParseResp parse(@RequestBody FileParseReq fileParseReq);

    @Slf4j
    @Component
    class LlmFeignFallBack implements FallbackFactory<LlmFeign> {

        @Override
        public LlmFeign create(Throwable cause) {
            log.error("模型调用出错", cause);
            return new LlmFeign() {
                @Override
                public FileParseResp parse(FileParseReq req) {
                    log.error("/api/file/parsing接口访问出错");
                    return null;
                }
            };
        }
    }
}

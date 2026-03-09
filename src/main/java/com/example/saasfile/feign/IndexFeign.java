package com.example.saasfile.feign;

import com.example.saasfile.feign.entity.index.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "index", url = "${service.index.address:http://localhost}", fallbackFactory = IndexFeign.IndexFeignFallBack.class)
public interface IndexFeign {

    @GetMapping("/indicator/report/excel/parsing-status")
    R<String> getReportParsingStatus(@RequestParam String fileId, @RequestParam String user_id);

    @Slf4j
    @Component
    class IndexFeignFallBack implements FallbackFactory<IndexFeign> {
        @Override
        public IndexFeign create(Throwable cause) {
            return new IndexFeign() {
                @Override
                public R<String> getReportParsingStatus(String fileId, String userId) {
                    log.error("Call IndexFeign#getReportParsingStatus failed", cause);
                    return null;
                }
            };
        }
    }
}

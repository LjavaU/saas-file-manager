package com.supcon.tptrecommend.feign;

import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.feign.entity.index.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "index", url = "${service.index.address:指标服务地址不能为空}", fallbackFactory = IndexFeign.IndexFeignFallBack.class)
public interface IndexFeign {

    /**
     * 获取报表解析状态
     * WAITING 等待解析
     * PARSING 解析中
     * COMPLETED 解析完成
     * ERROR 解析错误
     * PARTIAL_COMPLETION 部分完成
     *
     * @param fileId 文件 ID
     * @param user_id 用户 ID
     * @return {@link SupResult }<{@link String }>
     * @author luhao
     * @since 2025/07/24 13:58:35
     */
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
                    log.error("调用 IndexFeign#getReportParsingStatus 失败", cause);
                    return null;
                }
            };
        }
    }

}

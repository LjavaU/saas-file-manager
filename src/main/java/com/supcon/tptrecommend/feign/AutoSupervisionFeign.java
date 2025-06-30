package com.supcon.tptrecommend.feign;

import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.feign.entity.autosupervision.ControlExcelExport;
import com.supcon.tptrecommend.feign.entity.autosupervision.TagExcelImport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 自主监督服雾
 *
 * @author luhao
 * @since 2025/06/25 19:02:37
 */
@FeignClient(name = "autoSupervision", url = "${service.autoSupervision.address:自主监督地址不能为空}", fallbackFactory = AutoSupervisionFeign.AutoSupervisionFeignFallBack.class)
public interface AutoSupervisionFeign {

    /**
     * 导入回路信息
     *
     * @param body      请求体
     * @param groupType 组类型
     * @return {@link SupResult }<{@link List }<{@link ControlExcelExport }>>
     * @author luhao
     * @since 2025/06/25 19:02:29
     */
    @PostMapping("/api/outer/evaluationItemImport")
    SupResult<List<ControlExcelExport>> importBottomTagExcelOrigin(@RequestBody SupRequestBody<List<TagExcelImport>> body, @RequestParam("groupType") Integer groupType);

    @Slf4j
    @Component
    class AutoSupervisionFeignFallBack implements FallbackFactory<AutoSupervisionFeign> {

        @Override
        public AutoSupervisionFeign create(Throwable cause) {
            log.error("自主监督服务调用出错", cause);
            return new AutoSupervisionFeign() {

                @Override
                public SupResult<List<ControlExcelExport>> importBottomTagExcelOrigin(SupRequestBody<List<TagExcelImport>> body, Integer groupType) {
                    log.error("/api/outer/evaluationItemImport接口访问出错");
                    return SupResult.error("/api/outer/evaluationItemImport接口调用异常!");
                }
            };

        }
    }
}

package com.supcon.tptrecommend.openapi;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.supcon.system.base.common.annotation.UnAuthentication;
import com.supcon.system.base.entity.basic.BasicController;
import com.supcon.systembase.logapi.annotation.SysServiceLog;
import com.supcon.systembase.logapi.enums.OperateTypeEnum;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.dto.questionrecommend.HomeQuestionRecommendReq;
import com.supcon.tptrecommend.manager.QuestionRecommendManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@UnAuthentication
@RestController
@RequestMapping("/open-api/questionRecommend")
@Validated
@Api(tags = "问题推荐")
@RequiredArgsConstructor
public class QuestionRecommendController extends BasicController {

    private final QuestionRecommendManager questionRecommendManager;

    /**
     * 首页问题刷新
     *
     * @return {@link SupResult }<{@link List }<{@link String }>>
     * @author luhao
     * @date 2025-05-22
     */
    @PostMapping("refreshHome")
    @ApiOperation("首页问题刷新")
    @ApiOperationSupport(order = 1, author = "luhao")
    @SysServiceLog(moduleName = "首页问题刷新", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<Set<String>> queryPageList(@RequestBody SupRequestBody<HomeQuestionRecommendReq> req) {
        Set<String> questions = questionRecommendManager.refreshHomepageRecommendations(req.getData());
        return data(questions);
    }


}

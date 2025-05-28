package com.supcon.tptrecommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.supcon.system.base.entity.basic.BasicController;
import com.supcon.systembase.logapi.annotation.SysServiceLog;
import com.supcon.systembase.logapi.enums.OperateTypeEnum;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.systemcomponent.excel.annotation.RequestExcel;
import com.supcon.systemcomponent.excel.entity.ExcelReadResult;
import com.supcon.tptrecommend.convert.question.QuestionConvert;
import com.supcon.tptrecommend.dto.question.QuestionCreateReq;
import com.supcon.tptrecommend.dto.question.QuestionResp;
import com.supcon.tptrecommend.dto.question.QuestionUpdateReq;
import com.supcon.tptrecommend.entity.excel.QuestionImportExcel;
import com.supcon.tptrecommend.service.IQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * 问题主表控制器
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
@Validated
@Api(tags = "问题主表")
public class QuestionController extends BasicController {

    private final IQuestionService questionService;

    /**
     * <p>Title:多功能查询分页</p>
     *
     * @param body 请求体
     * @author luhao
     * @date 2025-05-22
     */
    @PostMapping("page")
    @ApiOperation("多功能查询分页")
    @ApiOperationSupport(order = 1, author = "luhao")
    @SysServiceLog(moduleName = "问题主表-分页列表查询", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<Page<QuestionResp>> queryPageList(@Valid @RequestBody SupRequestBody<Map<String, String>> body) throws Exception{
        return data((Page<QuestionResp>) questionService.pageAutoQuery(body).convert(QuestionConvert.INSTANCE::convert));
    }

    /**
     * <p>Title:查询单条</p>
     * <p>Description:</p>
     *
     * @param id 记录ID
     * @author luhao
     * @date 2025-05-22
     */
    @GetMapping("get/{id}")
    @ApiOperation("查询单条")
    @ApiOperationSupport(order = 2, author = "luhao")
    @SysServiceLog(moduleName = "问题主表-查询单条记录", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<QuestionResp> get(@NotNull(message = "{id}：{}") @PathVariable Long id) {
        return data(questionService.getObj(id));
    }

    /**
     * <p>Title:新增</p>
     * <p>Description:</p>
     *
     * @param body 请求体
     * @author luhao
     * @date 2025-05-22
     */
    @PostMapping("add")
    @ApiOperation("新增")
    @ApiOperationSupport(order = 3, author = "luhao")
    @SysServiceLog(moduleName = "问题主表-新增", operateType = OperateTypeEnum.LOG_TYPE_ADD)
    public SupResult<Long> add(@Valid @RequestBody SupRequestBody<QuestionCreateReq> body){
        Long id = questionService.saveObj(body.getData());
        return data(id);
    }

    /**
     * <p>Title:修改</p>
     * <p>Description:</p>
     *
     * @param body 请求体
     * @author luhao
     * @date 2025-05-22
     */
    @PutMapping("update")
    @ApiOperation("修改")
    @ApiOperationSupport(order = 4, author = "luhao")
    @SysServiceLog(moduleName = "问题主表-修改", operateType = OperateTypeEnum.LOG_TYPE_UPDATE)
    public SupResult<Boolean> update(@Valid @RequestBody SupRequestBody<QuestionUpdateReq> body){
        boolean isUpdate = questionService.updateObj(body.getData());
        return status(isUpdate);
    }

    /**
     * <p>Title:删除(默认物理删除)</p>
     *
     * @param body 请求体(传入ID集合)
     * @author luhao
     * @date 2025-05-22
     * @since 1.1.0
     */
    @DeleteMapping("batchDelete")
    @ApiOperation("删除")
    @ApiOperationSupport(order = 5, author = "luhao")
    @SysServiceLog(moduleName = "问题主表-删除多条记录", operateType = OperateTypeEnum.LOG_TYPE_DEL)
    public SupResult<Boolean> batchDelete(@Valid @RequestBody SupRequestBody<IDList<Long>> body){
        boolean isRemove = questionService.removeObjs(body.getData().getIds());
        return status(isRemove);
    }

    @GetMapping("templateDownload")
    @ApiOperation("导入模板下载")
    @ApiOperationSupport(order = 6, author = "zhaojun")
    @SysServiceLog(moduleName = "问题主表-导入模板下载", operateType = OperateTypeEnum.LOG_TYPE_DEL)
    public SupResult<Void> templateDownload(HttpServletResponse response) throws IOException {
        questionService.templateDownload(response);
       return SupResult.success();
    }


    @PostMapping("nonUserQuesImport")
    @ApiOperation("非用户问题导入")
    @ApiOperationSupport(order = 7, author = "zhaojun")
    @SysServiceLog(moduleName = "二次位号管理-Excel导入", operateType = OperateTypeEnum.LOG_TYPE_ADD)
    public SupResult<String> nonUserQuesImport(@RequestExcel(isSaveErrorExcel = true) @ApiIgnore ExcelReadResult<QuestionImportExcel> excelReadResult) {
        if (!excelReadResult.isSuccess()) {
            return SupResult.errorWithContent(excelReadResult.getFailFilePath(), "导入失败");
        }
        questionService.nonUserQuesImport(excelReadResult.getSucData());
        return SupResult.success();
    }

    @PostMapping("userQuesImport")
    @ApiOperation("用户问题导入")
    @ApiOperationSupport(order = 8, author = "zhaojun")
    @SysServiceLog(moduleName = "二次位号管理-Excel导入", operateType = OperateTypeEnum.LOG_TYPE_ADD)
    public SupResult<String> userQuesImport(@RequestExcel(isSaveErrorExcel = true) @ApiIgnore ExcelReadResult<QuestionImportExcel> excelReadResult) {
        if (!excelReadResult.isSuccess()) {
            return SupResult.errorWithContent(excelReadResult.getFailFilePath(), "导入失败");
        }
        questionService.userQuesImport(excelReadResult.getSucData());
        return SupResult.success();
    }


}

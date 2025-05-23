package com.supcon.tptrecommend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.supcon.system.base.entity.basic.BasicController;
import com.supcon.systembase.logapi.annotation.SysServiceLog;
import com.supcon.systembase.logapi.enums.OperateTypeEnum;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.convert.userinfo.UserInfoConvert;
import com.supcon.tptrecommend.dto.userinfo.UserInfoCreateReq;
import com.supcon.tptrecommend.dto.userinfo.UserInfoResp;
import com.supcon.tptrecommend.dto.userinfo.UserInfoUpdateReq;
import com.supcon.tptrecommend.service.IUserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表控制器
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@RestController
@RequestMapping("/api/user-info")
@RequiredArgsConstructor
@Validated
@Api(tags = "用户信息表")
public class UserInfoController extends BasicController {

    private final IUserInfoService userInfoService;

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
    @SysServiceLog(moduleName = "用户信息表-分页列表查询", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<Page<UserInfoResp>> queryPageList(@Valid @RequestBody SupRequestBody<Map<String, String>> body) throws Exception {
        return data((Page<UserInfoResp>) userInfoService.pageAutoQuery(body).convert(UserInfoConvert.INSTANCE::convert));
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
    @SysServiceLog(moduleName = "用户信息表-查询单条记录", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<UserInfoResp> get(@NotNull(message = "{id}：{}") @PathVariable Long id) {
        return data(userInfoService.getObj(id));
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
    @SysServiceLog(moduleName = "用户信息表-新增", operateType = OperateTypeEnum.LOG_TYPE_ADD)
    public SupResult<Long> add(@Valid @RequestBody SupRequestBody<UserInfoCreateReq> body) {
        Long id = userInfoService.saveObj(body.getData());
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
    @SysServiceLog(moduleName = "用户信息表-修改", operateType = OperateTypeEnum.LOG_TYPE_UPDATE)
    public SupResult<Boolean> update(@Valid @RequestBody SupRequestBody<UserInfoUpdateReq> body) {
        boolean isUpdate = userInfoService.updateObj(body.getData());
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
    @SysServiceLog(moduleName = "用户信息表-删除多条记录", operateType = OperateTypeEnum.LOG_TYPE_DEL)
    public SupResult<Boolean> batchDelete(@Valid @RequestBody SupRequestBody<IDList<Long>> body) {
        boolean isRemove = userInfoService.removeObjs(body.getData().getIds());
        return status(isRemove);
    }

    @PostMapping("sync")
    @ApiOperation("sass平台同步用户信息")
    @ApiOperationSupport(order = 6, author = "luhao")
    @SysServiceLog(moduleName = "用户信息表-sass平台同步用户信息", operateType = OperateTypeEnum.LOG_TYPE_ADD)
    public SupResult<Boolean> sync(@Valid @RequestBody SupRequestBody<List<UserInfoCreateReq>> body) {
        userInfoService.sync(body.getData());
        return data(true);
    }
}

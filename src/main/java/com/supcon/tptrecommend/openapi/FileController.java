package com.supcon.tptrecommend.openapi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.supcon.system.base.entity.basic.BasicController;
import com.supcon.systembase.logapi.annotation.SysServiceLog;
import com.supcon.systembase.logapi.enums.OperateTypeEnum;
import com.supcon.systemcommon.entity.IDList;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.dto.fileobject.*;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.manager.FileManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/open-api/file")
@RequiredArgsConstructor
@Validated
@Api(tags = "文件管理")
public class FileController extends BasicController {

    private final FileManager fileManager;

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @return 结果
     * @author luhao
     * @date 2025/05/22 13:52:19
     */
    @PostMapping(value = "/upload")
    @ApiOperation("上传文件")
    @ApiOperationSupport(order = 1, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-上传文件", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<FileObjectResp> upload(@RequestPart(value = "file") MultipartFile multipartFile, String attributes, String path) {
        return data(fileManager.upload(multipartFile, attributes, path));
    }

    @DeleteMapping(value = "delete/{id}")
    @ApiOperation("删除文件")
    @ApiOperationSupport(order = 2, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-删除文件", operateType = OperateTypeEnum.LOG_TYPE_DEL, onlyExceptions = true)
    public SupResult<Boolean> delete(@PathVariable Long id) {
        Boolean res = fileManager.delete(id);
        return data(res);
    }

    @DeleteMapping("batchDelete")
    @ApiOperation("批量删除文件")
    @ApiOperationSupport(order = 3, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-批量删除文件", operateType = OperateTypeEnum.LOG_TYPE_DEL, onlyExceptions = true)
    public SupResult<Boolean> batchDelete(@Valid @RequestBody SupRequestBody<IDList<Long>> body) {
        Boolean res = fileManager.batchDelete(body.getData());
        return data(res);
    }

    @PostMapping("page")
    @ApiOperation("文件列表查询")
    @ApiOperationSupport(order = 4, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-分页列表查询", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<IPage<FileObjectResp>> queryPageList(@Valid @RequestBody SupRequestBody<Map<String, String>> body) throws Exception {
        return data(fileManager.selectPage(body));
    }


    @PostMapping("getOne")
    @ApiOperation("获取单个文件流")
    @ApiOperationSupport(order = 5, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-获取单个文件流", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public void getOne(@Valid @RequestBody SupRequestBody<SingleFileQueryReq> req, HttpServletResponse response) {
        fileManager.getOne(req.getData(), response);
    }


    @PostMapping("detail")
    @ApiOperation("获取文件详情")
    @ApiOperationSupport(order = 6, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-获取文件详情", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<List<FileObject>> detail(@RequestBody @Valid  SupRequestBody<FileDetailReq> req) {
        return data(fileManager.detail(req.getData()));
    }

    @PostMapping("createFolder")
    @ApiOperation("创建文件夹")
    @ApiOperationSupport(order = 7, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-获取单个文件流", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<Boolean> createFolder(@RequestBody @Valid SupRequestBody<CreateFolderReq> req) {
        return data(fileManager.createFolder(req.getData()));
    }

    @GetMapping("browse")
    @ApiOperation("获取文件层级结构")
    @ApiOperationSupport(order = 8, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-获取文件层级结构", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<List<FileNodeResp>> browse(@RequestParam(value = "path", defaultValue = "") String path) {
        return data(fileManager.listFiles(path));
    }

    @GetMapping("tree")
    @ApiOperation("获取文件树形结构")
    @ApiOperationSupport(order = 9, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-获取文件树形结构", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<FileTreeNode> tree() {
        return data(fileManager.listFilesAsTree());
    }

    @PostMapping("update")
    @ApiOperation("更新文件属性")
    @ApiOperationSupport(order = 10, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-更新文件属性", operateType = OperateTypeEnum.LOG_TYPE_UPDATE, onlyExceptions = true)
    public SupResult<Boolean> update(@RequestBody SupRequestBody<FileAttributesUpdatedReq> req) {
        return data(fileManager.update(req.getData()));
    }
}

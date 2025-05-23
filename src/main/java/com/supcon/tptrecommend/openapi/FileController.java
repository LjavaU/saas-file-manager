package com.supcon.tptrecommend.openapi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.supcon.system.base.entity.basic.BasicController;
import com.supcon.systembase.logapi.annotation.SysServiceLog;
import com.supcon.systembase.logapi.enums.OperateTypeEnum;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.manager.FileManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Map;

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
    @SysServiceLog(moduleName = "文件管理-上传文件", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<Long> upload(@RequestPart(value = "file") MultipartFile multipartFile) {
        Long res = fileManager.upload(multipartFile);
        return data(res);
    }

    /**
     * 上传文件
     *
     * @param id 身份证
     * @return 结果
     * @author luhao
     * @date 2025/05/22 13:52:19
     */
    @DeleteMapping(value = "delete/{id}")
    @ApiOperation("删除文件")
    @ApiOperationSupport(order = 2, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-删除文件", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<Boolean> delete(@PathVariable Long id) {
        Boolean res = fileManager.delete(id);
        return data(res);
    }

    @PostMapping("page")
    @ApiOperation("文件列表查询")
    @ApiOperationSupport(order = 3, author = "luhao")
    @SysServiceLog(moduleName = "文件管理-分页列表查询", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<IPage<FileObjectResp>> queryPageList(@Valid @RequestBody SupRequestBody<Map<String, String>> body) throws Exception{
        return data(fileManager.selectPage(body));
    }



}

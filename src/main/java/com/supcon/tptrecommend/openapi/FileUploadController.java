package com.supcon.tptrecommend.openapi;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.supcon.system.base.entity.basic.BasicController;
import com.supcon.systembase.logapi.annotation.SysServiceLog;
import com.supcon.systembase.logapi.enums.OperateTypeEnum;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.dto.fileUpload.*;
import com.supcon.tptrecommend.manager.FileUploadDirectManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/open-api/file")
@RequiredArgsConstructor
@Validated
@Api(tags = "文件直传管理")
public class FileUploadController extends BasicController {

    private final FileUploadDirectManager fileUploadDirectManager;

    @PostMapping("/presigned-url")
    @ApiOperation("普通预签名上传-初始化")
    @ApiOperationSupport(order = 20, author = "codex")
    @SysServiceLog(moduleName = "文件直传-普通预签名初始化", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<PresignedUploadResp> presignedUrl(@Valid @RequestBody PresignedUploadInitReq req) {
        return data(fileUploadDirectManager.createPresignedUploadUrl(req));
    }

    @PostMapping("/upload-callback")
    @ApiOperation("普通预签名上传-回调")
    @ApiOperationSupport(order = 21, author = "codex")
    @SysServiceLog(moduleName = "文件直传-普通上传回调", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<UploadCompleteResp> uploadCallback(@Valid @RequestBody UploadCallbackReq req) {
        return data(fileUploadDirectManager.uploadCallback(req));
    }

    @PostMapping("/multipart/init")
    @ApiOperation("分片上传-初始化")
    @ApiOperationSupport(order = 22, author = "codex")
    @SysServiceLog(moduleName = "文件直传-分片初始化", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<MultipartUploadInitResp> multipartInit(@Valid @RequestBody MultipartUploadInitReq req) {
        return data(fileUploadDirectManager.initMultipartUpload(req));
    }

    @PostMapping("/multipart/sign")
    @ApiOperation("分片上传-分片签名")
    @ApiOperationSupport(order = 23, author = "codex")
    @SysServiceLog(moduleName = "文件直传-分片签名", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<MultipartUploadSignResp> multipartSign(@Valid @RequestBody MultipartUploadSignReq req) {
        return data(fileUploadDirectManager.signMultipartPart(req));
    }

    @PostMapping("/multipart/complete")
    @ApiOperation("分片上传-完成合并")
    @ApiOperationSupport(order = 24, author = "codex")
    @SysServiceLog(moduleName = "文件直传-分片完成", operateType = OperateTypeEnum.LOG_TYPE_LOOK, onlyExceptions = true)
    public SupResult<UploadCompleteResp> multipartComplete(@Valid @RequestBody MultipartUploadCompleteReq req) {
        return data(fileUploadDirectManager.completeMultipartUpload(req));
    }
}

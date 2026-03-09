package com.example.saasfile.openapi;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.example.saasfile.support.web.BasicController;
import com.example.saasfile.support.log.SysServiceLog;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.dto.fileUpload.*;
import com.example.saasfile.manager.FileUploadDirectManager;
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
@Api(tags = "")
public class FileUploadController extends BasicController {

    private final FileUploadDirectManager fileUploadDirectManager;

    @PostMapping("/presigned-url")
    @ApiOperation("")
    @ApiOperationSupport(order = 20, author = "codex")
    @SysServiceLog(moduleName = "")
    public SupResult<PresignedUploadResp> presignedUrl(@Valid @RequestBody PresignedUploadInitReq req) {
        return data(fileUploadDirectManager.createPresignedUploadUrl(req));
    }

    @PostMapping("/upload-callback")
    @ApiOperation("")
    @ApiOperationSupport(order = 21, author = "codex")
    @SysServiceLog(moduleName = "")
    public SupResult<UploadCompleteResp> uploadCallback(@Valid @RequestBody UploadCallbackReq req) {
        return data(fileUploadDirectManager.uploadCallback(req));
    }

    @PostMapping("/multipart/init")
    @ApiOperation("")
    @ApiOperationSupport(order = 22, author = "codex")
    @SysServiceLog(moduleName = "")
    public SupResult<MultipartUploadInitResp> multipartInit(@Valid @RequestBody MultipartUploadInitReq req) {
        return data(fileUploadDirectManager.initMultipartUpload(req));
    }

    @PostMapping("/multipart/sign")
    @ApiOperation("")
    @ApiOperationSupport(order = 23, author = "codex")
    @SysServiceLog(moduleName = "")
    public SupResult<MultipartUploadSignResp> multipartSign(@Valid @RequestBody MultipartUploadSignReq req) {
        return data(fileUploadDirectManager.signMultipartPart(req));
    }

    @PostMapping("/multipart/complete")
    @ApiOperation("")
    @ApiOperationSupport(order = 24, author = "codex")
    @SysServiceLog(moduleName = "")
    public SupResult<UploadCompleteResp> multipartComplete(@Valid @RequestBody MultipartUploadCompleteReq req) {
        return data(fileUploadDirectManager.completeMultipartUpload(req));
    }
}

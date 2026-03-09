package com.example.saasfile.openapi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.example.saasfile.support.auth.UnAuthentication;
import com.example.saasfile.support.web.BasicController;
import com.example.saasfile.support.log.SysServiceLog;
import com.example.saasfile.support.web.IDList;
import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.dto.fileUpload.ExcelUploadRequest;
import com.example.saasfile.dto.fileobject.*;
import com.example.saasfile.dto.fileshare.FileShareRequest;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.manager.FileManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/open-api/file")
@RequiredArgsConstructor
@Validated
@Api(tags = "")
public class FileController extends BasicController {

    private final FileManager fileManager;

    
    @PostMapping(value = "/upload")
    @ApiOperation("")
    @ApiOperationSupport(order = 1, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<FileObjectResp> upload(@RequestPart(value = "file") MultipartFile multipartFile, String path) {
        return data(fileManager.upload(multipartFile, path));
    }

    @DeleteMapping(value = "delete/{id}")
    @ApiOperation("")
    @ApiOperationSupport(order = 2, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Boolean> delete(@PathVariable Long id) {
        Boolean res = fileManager.delete(id);
        return data(res);
    }

    @DeleteMapping("batchDelete")
    @ApiOperation("")
    @ApiOperationSupport(order = 3, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Boolean> batchDelete(@Valid @RequestBody SupRequestBody<IDList<Long>> body) {
        Boolean res = fileManager.batchDelete(body.getData());
        return data(res);
    }

    @PostMapping("page")
    @ApiOperation("")
    @ApiOperationSupport(order = 4, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<IPage<FileObjectResp>> queryPageList(@Valid @RequestBody SupRequestBody<Map<String, String>> body) throws Exception {
        return data(fileManager.selectPage(body));
    }


    @PostMapping("getOne")
    @ApiOperation("")
    @ApiOperationSupport(order = 5, author = "luhao")
    @SysServiceLog(moduleName = "")
    @UnAuthentication
    public void getOne(@Valid @RequestBody SupRequestBody<SingleFileQueryReq> req, HttpServletResponse response) {
        fileManager.getOne(req.getData(), response);
    }


    @PostMapping("detail")
    @ApiOperation("")
    @ApiOperationSupport(order = 6, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<List<FileObject>> detail(@RequestBody @Valid SupRequestBody<FileDetailReq> req) {
        return data(fileManager.detail(req.getData()));
    }

    @PostMapping("createFolder")
    @ApiOperation("")
    @ApiOperationSupport(order = 7, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Boolean> createFolder(@RequestBody @Valid SupRequestBody<CreateFolderReq> req) {
        return data(fileManager.createFolder(req.getData()));
    }

    @GetMapping("browse")
    @ApiOperation("")
    @ApiOperationSupport(order = 8, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<List<FileNodeResp>> browse(@RequestParam(value = "path", defaultValue = "") String path) {
        return data(fileManager.listFiles(path));
    }

    @GetMapping("tree")
    @ApiOperation("")
    @ApiOperationSupport(order = 9, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<FileTreeNode> tree() {
        return data(fileManager.listFilesAsTree());
    }

    @PostMapping("update")
    @UnAuthentication
    @ApiOperation("")
    @ApiOperationSupport(order = 10, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Boolean> update(@RequestBody SupRequestBody<FileAttributesUpdatedReq> req) {
        return data(fileManager.update(req.getData()));
    }

    @GetMapping("getFileStatus/{fileId}")
    @ApiOperation("")
    @ApiOperationSupport(order = 11, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Integer> getFileStatus(@PathVariable Long fileId) {
        return data(fileManager.getFileStatus(fileId));
    }

    @GetMapping("reIndexParse/{fileId}")
    @ApiOperation("")
    @ApiOperationSupport(order = 12, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Integer> reIndexParse(@PathVariable Long fileId) {
        fileManager.reIndexParse(fileId);
        return SupResult.success();
    }


    @PostMapping("convertFileToUpload")
    @ApiOperation("")
    @ApiOperationSupport(order = 13, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Integer> convertFileToUpload(@RequestBody ExcelUploadRequest request) {
        fileManager.convertFileToUpload(request);
        return SupResult.success();

    }


    
    @GetMapping("statistics")
    @ApiOperation("")
    @ApiOperationSupport(order = 14, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<FileStatisticsResp> fileStatistics() {
        return data(fileManager.fileStatistics());
    }


    
    @PostMapping(value = "/batchUpload")
    @ApiOperation("")
    @ApiOperationSupport(order = 15, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<List<FileObjectResp>> batchUpload(@RequestPart(value = "files") List<MultipartFile> multipartFiles, String path) {
        return data(fileManager.batchUpload(multipartFiles, path));
    }


    @GetMapping(value = "/downloadFilesAsZip")
    @UnAuthentication
    @ApiOperation("")
    @ApiOperationSupport(order = 16, author = "luhao")
    @SysServiceLog(moduleName = "")
    public void downloadTenantFilesAsZip(String tenantId,String userName, HttpServletResponse response) {
        fileManager.downloadTenantFilesAsZip(tenantId,userName, response);

    }

    @PostMapping("/share-link")
    @ApiOperation("")
    @UnAuthentication
    @ApiOperationSupport(order = 17, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<String> getShareLink(@Validated @RequestBody FileShareRequest request) {
        return data(fileManager.createShareLink(request));
    }

    @GetMapping("/link-download")
    @ApiOperation("")
    @UnAuthentication
    @ApiOperationSupport(order = 18, author = "luhao")
    @SysServiceLog(moduleName = "")
    public ResponseEntity<StreamingResponseBody> linkDownload(@RequestParam String ticket) {
        return fileManager.linkDownload(ticket);
    }


    @GetMapping("reParse/{fileId}")
    @ApiOperation("")
    @ApiOperationSupport(order = 19, author = "luhao")
    @SysServiceLog(moduleName = "")
    public SupResult<Integer> reParse(@PathVariable Long fileId) {
        fileManager.reParse(fileId);
        return SupResult.success();
    }



}

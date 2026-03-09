package com.example.saasfile.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.saasfile.support.web.IDList;
import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.dto.fileUpload.ExcelUploadRequest;
import com.example.saasfile.dto.fileobject.*;
import com.example.saasfile.dto.fileshare.FileShareRequest;
import com.example.saasfile.entity.FileObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

public interface FileManager {
    
    FileObjectResp upload(MultipartFile multipartFile,String path);

    
    Boolean delete(Long id);

    
    IPage<FileObjectResp>  selectPage(@Valid SupRequestBody<Map<String, String>> body) throws Exception;

    
    void getOne(SingleFileQueryReq req, HttpServletResponse response);



    List<FileObject> detail(FileDetailReq fileId);


    
    Boolean batchDelete(IDList<Long> data);

    
    boolean createFolder(CreateFolderReq data);


    
    List<FileNodeResp> listFiles(String path);

    
    FileTreeNode listFilesAsTree();


    
    boolean update(FileAttributesUpdatedReq req);

    
    Integer getFileStatus(Long fileId);

    
    void reIndexParse(Long fileId);

    
    void convertFileToUpload(ExcelUploadRequest request);

    
    FileStatisticsResp fileStatistics();

    List<FileObjectResp> batchUpload(List<MultipartFile> multipartFiles, String path);

    void downloadTenantFilesAsZip(String tenantId, String userName, HttpServletResponse response);


    
    String createShareLink(FileShareRequest request);

    
    ResponseEntity<StreamingResponseBody> linkDownload(String ticket);

    void reParse(Long fileId);
}

package com.example.saasfile.service;

import com.example.saasfile.support.mybatis.IBasicService;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.dto.fileobject.FileAttributesUpdatedCondition;
import com.example.saasfile.dto.fileobject.FileObjectCreateReq;
import com.example.saasfile.dto.fileobject.FileObjectResp;
import com.example.saasfile.dto.fileobject.FileStatisticsResp;
import com.example.saasfile.entity.FileObject;

import java.util.List;


public interface IFileObjectService extends IBasicService<FileObject> {


    
    Long saveObj(FileObjectCreateReq fileObjectCreateReq);

    
    boolean updateFileParseStatus(Long fileId, FileStatus fileStatus);

    
    List<FileObjectResp> getKnowledgeParsing(Integer knowledgeParseState);

    
    void updateKnowledgeParseState(FileObject fileObject);

    
    Long getUserIdByFileId(Long fileId);

    FileStatisticsResp getFileStatistics();

    
    FileObject getByObjectName(String objectName);


    
    void updateFileAttributes(FileAttributesUpdatedCondition updatedCondition);
}

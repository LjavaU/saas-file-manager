package com.example.saasfile.service.impl;

import com.example.saasfile.support.mybatis.BasicServiceImpl;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.convert.fileobject.FileObjectConvert;
import com.example.saasfile.dto.fileobject.FileAttributesUpdatedCondition;
import com.example.saasfile.dto.fileobject.FileObjectCreateReq;
import com.example.saasfile.dto.fileobject.FileObjectResp;
import com.example.saasfile.dto.fileobject.FileStatisticsResp;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.mapper.FileObjectMapper;
import com.example.saasfile.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FileObjectServiceImpl extends BasicServiceImpl<FileObjectMapper, FileObject> implements IFileObjectService {

    private final FileObjectMapper fileObjectMapper;


    @Override
    public Long saveObj(FileObjectCreateReq fileObjectCreateReq) {
        FileObject fileObject = FileObjectConvert.INSTANCE.convert(fileObjectCreateReq);
        fileObjectMapper.insert(fileObject);
        return fileObject.getId();
    }

    public boolean updateFileParseStatus(Long fileId, FileStatus fileStatus) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(fileStatus.getValue());
        fileObjectMapper.updateById(fileObject);
        return true;
    }

    
    @Override
    public List<FileObjectResp> getKnowledgeParsing(Integer knowledgeParseState) {
        return fileObjectMapper.getKnowledgeParsing(knowledgeParseState);
    }

    
    @Override
    public void updateKnowledgeParseState(FileObject fileObject) {
        fileObjectMapper.updateKnowledgeParseState(fileObject);
    }

    
    @Override
    public Long getUserIdByFileId(Long fileId) {
        FileObject fileObject = fileObjectMapper.selectById(fileId);
        return fileObject == null ? null : fileObject.getUserId();
    }

    
    @Override
    public FileStatisticsResp getFileStatistics() {
        return fileObjectMapper.getFileStatistics();
    }

    @Override
    public FileObject getByObjectName(String objectName) {
       return fileObjectMapper.getByObjectName(objectName);
    }

    @Override
    public void updateFileAttributes(FileAttributesUpdatedCondition updatedCondition) {
        fileObjectMapper.updateFileAttributes(updatedCondition);
    }

}

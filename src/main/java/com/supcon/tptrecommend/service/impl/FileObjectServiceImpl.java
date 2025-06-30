package com.supcon.tptrecommend.service.impl;

import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.mapper.FileObjectMapper;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * MinIO 文件元数据表 服务实现类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
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

    public boolean updateFileParseStatus(Long fileId, FileObject.FileStatus fileStatus) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setFileStatus(fileStatus.getValue());
        fileObjectMapper.updateById(fileObject);
        return true;
    }


}

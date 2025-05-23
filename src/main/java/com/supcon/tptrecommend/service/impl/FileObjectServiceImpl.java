package com.supcon.tptrecommend.service.impl;

import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.FileObjectUpdateReq;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.mapper.FileObjectMapper;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public FileObjectResp getObj(Long id) {
        FileObject fileObject = fileObjectMapper.selectById(id);
        return FileObjectConvert.INSTANCE.convert(fileObject);
    }

    @Override
    public Long saveObj(FileObjectCreateReq fileObjectCreateReq) {
        FileObject fileObject = FileObjectConvert.INSTANCE.convert(fileObjectCreateReq);
        fileObjectMapper.insert(fileObject);
        return fileObject.getId();
    }

    @Override
    public boolean updateObj(FileObjectUpdateReq fileObjectUpdateReq) {
        FileObject fileObject = FileObjectConvert.INSTANCE.convert(fileObjectUpdateReq);
        return fileObjectMapper.updateById(fileObject) > 0;
    }

    @Override
    public boolean removeObjs(List<Long> ids) {
        return fileObjectMapper.deleteBatchIds(ids) > 0;
    }

}

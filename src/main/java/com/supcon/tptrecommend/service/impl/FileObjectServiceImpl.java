package com.supcon.tptrecommend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.supcon.system.base.entity.AutoIdEntity;
import com.supcon.system.base.entity.basic.impl.BasicServiceImpl;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.convert.fileobject.FileObjectConvert;
import com.supcon.tptrecommend.dto.fileobject.FileAttributesUpdatedCondition;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.FileStatisticsResp;
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

    /**
     * 获取正在解析的知识库文件
     *
     * @return {@link List }<{@link FileObjectResp }>
     * @author luhao
     * @since 2025/07/30 11:27:40
     */
    @Override
    public List<FileObjectResp> getKnowledgeParsing(Integer knowledgeParseState) {
        return fileObjectMapper.getKnowledgeParsing(knowledgeParseState);
    }

    /**
     * 更新知识库解析状态
     *
     * @param fileObject 文件对象
     * @author luhao
     * @since 2025/07/30 13:27:50
     */
    @Override
    public void updateKnowledgeParseState(FileObject fileObject) {
        fileObjectMapper.updateKnowledgeParseState(fileObject);
    }

    /**
     * 通过文件 ID 获取用户 ID
     *
     * @param fileId 文件 ID
     * @return {@link Long }
     * @author luhao
     * @since 2025/08/11 15:17:23
     *
     */
    @Override
    public Long getUserIdByFileId(Long fileId) {
        return getObj(Wrappers.<FileObject>lambdaQuery()
            .select(FileObject::getUserId)
            .eq(AutoIdEntity::getId, fileId), obj -> {
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            }
            return null;
        });
    }

    /**
     * 统计文件总数和总大小
     *
     * @return {@link FileStatisticsResp }
     * @author luhao
     * @since 2025/09/10 14:46:05
     *
     */
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

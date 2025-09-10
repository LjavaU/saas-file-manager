package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.FileStatisticsResp;
import com.supcon.tptrecommend.entity.FileObject;

import java.util.List;

/**
 * <p>
 * MinIO 文件元数据表 服务类
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
public interface IFileObjectService extends IBasicService<FileObject> {


    /**
     * 保存
     *
     * @param fileObjectCreateReq 对象创建实体
     * @return 对象ID
     * @author luhao
     * @date 2025-05-22
     */
    Long saveObj(FileObjectCreateReq fileObjectCreateReq);

    /**
     * 更新文件解析状态
     *
     * @param fileId     文件 ID
     * @param fileStatus 文件状态
     * @return boolean
     * @author luhao
     * @since 2025/06/30 14:54:24
     */
    boolean updateFileParseStatus(Long fileId, FileStatus fileStatus);

    /**
     * 获取正在解析的知识库文件
     *
     * @return {@link List }<{@link FileObjectResp }>
     * @author luhao
     * @since 2025/07/30 11:27:12
     */
    List<FileObjectResp> getKnowledgeParsing(Integer knowledgeParseState);

    /**
     * 更新知识库解析状态
     *
     * @param fileObject 文件对象
     * @author luhao
     * @since 2025/07/30 13:27:32
     */
    void updateKnowledgeParseState(FileObject fileObject);

    /**
     * 通过文件 ID 获取用户 ID
     *
     * @param fileId 文件 ID
     * @return {@link Long }
     * @author luhao
     * @since 2025/08/11 15:17:18
     *
     */
    Long getUserIdByFileId(Long fileId);

    FileStatisticsResp getFileStatistics();
}

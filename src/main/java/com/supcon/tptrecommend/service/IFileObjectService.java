package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.entity.FileObject;

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

}

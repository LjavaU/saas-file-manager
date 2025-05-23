package com.supcon.tptrecommend.service;

import com.supcon.system.base.entity.basic.IBasicService;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.dto.fileobject.FileObjectUpdateReq;
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
     * 获取
     *
     * @param id 对象ID
     * @return 对象响应实体
     * @author luhao
     * @date 2025-05-22
     */
    FileObjectResp getObj(Long id);

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
     * 更新
     *
     * @param fileObjectUpdateReq 对象更新实体
     * @return 更新是否成功
     * @author luhao
     * @date 2025-05-22
     */
    boolean updateObj(FileObjectUpdateReq fileObjectUpdateReq);


    /**
     * 批量删除
     *
     * @param ids 对象ID集合
     * @return 批量删除时是否成功
     * @author luhao
     * @date 2025-05-22
     */
    boolean removeObjs(List<Long> ids);
}

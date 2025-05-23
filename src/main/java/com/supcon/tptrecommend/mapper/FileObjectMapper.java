package com.supcon.tptrecommend.mapper;

import com.supcon.system.base.entity.basic.IBaseMapper;
import com.supcon.tptrecommend.entity.FileObject;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * MinIO 文件元数据表 Mapper 接口
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Mapper
public interface FileObjectMapper extends IBaseMapper<FileObject> {

}

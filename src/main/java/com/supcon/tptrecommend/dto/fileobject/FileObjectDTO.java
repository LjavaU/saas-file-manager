package com.supcon.tptrecommend.dto.fileobject;

import com.supcon.tptrecommend.entity.FileObject;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

/**
 * <p>
 * MinIO 文件元数据表
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel(value = "MinIO 文件元数据表-数据模型", description = "MinIO 文件元数据表")
public class FileObjectDTO extends FileObject {

    private static final long serialVersionUID = 1L;


    /**
     * <p>Title:获取entity对象</p>
     * <p>Description:</p>
     * @author luhao
     * @date 2025-05-22
     * @return entity对象
     */
    public FileObject toFileObject() {
        FileObject fileObject = new FileObject();
        BeanUtils.copyProperties(this, fileObject);
        return fileObject;
    }
}
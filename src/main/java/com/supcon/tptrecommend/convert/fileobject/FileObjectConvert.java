package com.supcon.tptrecommend.convert.fileobject;

import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.entity.FileObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * MinIO 文件元数据表转换器
 * </p>
 *
 * @author luhao
 * @version 1.0.0
 * @date 2025-05-22
 */
@Mapper
public interface FileObjectConvert {

    FileObjectConvert INSTANCE = Mappers.getMapper(FileObjectConvert.class);

    FileObject convert(FileObjectCreateReq fileObjectCreateReq);

    @Mapping(target = "fileSize", expression = "java(mapFileSize(fileObject.getFileSize()))")
    FileObjectResp convert(FileObject fileObject);

    // 自定义方法
    default BigDecimal mapFileSize(Long fileSize) {
        BigDecimal divide = BigDecimal.valueOf(fileSize).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
        if (divide.equals(BigDecimal.ZERO)) {
            return BigDecimal.valueOf(fileSize).divide(BigDecimal.valueOf(1024 * 1024), 4, RoundingMode.HALF_UP);
        }
        return divide;

    }
}
package com.supcon.tptrecommend.convert.filedata;

import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.enums.TagDataTypeEnum;
import com.supcon.tptrecommend.feign.entity.datahub.TagInfoCreateReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

/**
 * 文件位号数据实体映射转换
 *
 * @author luhao
 * @since 2025/07/15 16:27:15
 */
@Mapper(componentModel = "spring")
public interface TagDataMappingConvert extends DynamicMapper<Map<String, String>, TagInfoCreateReq>  {


    @Override
    @Mapping(source = "dataType", target = "dataType", qualifiedByName = "dataTypeToCode")
    @Mapping(source = "onlyRead", target = "onlyRead", qualifiedByName = "onlyReadConvert")
    TagInfoCreateReq map(Map<String, String> source);


    /**
     * 位号数据类型转换
     *
     * @param dataType 数据类型
     * @return {@link Integer }
     * @author luhao
     * @since 2025/07/16 11:13:37
     */
    @Named("dataTypeToCode")
    default Integer dataTypeToCode(String dataType) {
        return TagDataTypeEnum.fromType(dataType);
    }

    /**
     * 位号是否只读转换
     *
     * @param onlyRead 仅阅读
     * @return {@link Boolean }
     * @author luhao
     * @since 2025/07/16 11:13:49
     */
    @Named("onlyReadConvert")
    default Boolean onlyReadConvert(String onlyRead) {
        return Boolean.parseBoolean(onlyRead);

    }

    @Override
    default Integer getIdentifier() {
        return SubCategoryEnum.TAG_CONFIGURATION.getCode();
    }
}

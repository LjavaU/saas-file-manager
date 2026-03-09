package com.example.saasfile.convert.filedata;

import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.enums.TagDataTypeEnum;
import com.example.saasfile.feign.entity.datahub.TagInfoCreateReq;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;


@Mapper(componentModel = "spring")
public interface TagDataMappingConvert extends DynamicMapper<Map<String, String>, TagInfoCreateReq>  {


    @Override
    @Mapping(source = "dataType", target = "dataType", qualifiedByName = "dataTypeToCode")
    @Mapping(source = "onlyRead", target = "onlyRead", qualifiedByName = "onlyReadConvert")
    TagInfoCreateReq map(Map<String, String> source);


    
    @Named("dataTypeToCode")
    default Integer dataTypeToCode(String dataType) {
        return TagDataTypeEnum.fromType(dataType);
    }

    
    @Named("onlyReadConvert")
    default Boolean onlyReadConvert(String onlyRead) {
        return Boolean.parseBoolean(onlyRead);

    }

    @Override
    default Integer getIdentifier() {
        return SubCategoryEnum.TAG_CONFIGURATION.getCode();
    }
}

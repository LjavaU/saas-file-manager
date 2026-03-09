package com.example.saasfile.convert.fileobject;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.example.saasfile.common.enums.FileCategoryAbilityAssociation;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.enums.TagHistoryCategory;
import com.example.saasfile.common.utils.FileUtils;
import com.example.saasfile.dto.fileobject.FileObjectCreateReq;
import com.example.saasfile.dto.fileobject.FileObjectResp;
import com.example.saasfile.entity.FileObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Mapper
public interface FileObjectConvert {

    FileObjectConvert INSTANCE = Mappers.getMapper(FileObjectConvert.class);

    FileObject convert(FileObjectCreateReq fileObjectCreateReq);

    @Mapping(target = "fileSize", expression = "java(mapFileSize(fileObject.getFileSize()))")
    @Mapping(target = "category", expression = "java(mapThirdCategory(fileObject))")
    @Mapping(target = "firstCategory", source = "category")
    @Mapping(target = "secondCategory", expression = "java(mapSecondCategory(fileObject))")
    @Mapping(target = "categoryIdentifier", expression = "java(mapCategoryIdentifier(fileObject))")
    FileObjectResp convert(FileObject fileObject);

    default String mapFileSize(Long fileSize) {
        return FileUtils.formatFileSize(fileSize);

    }

    default String mapCategory(FileObject fileObject) {

        String thirdCategory = mapThirdCategory(fileObject);
        if (thirdCategory != null) {
            return thirdCategory;
        }
        String secondCategory = mapSecondCategory(fileObject);
        if (secondCategory != null) {
            return secondCategory;
        }
        return fileObject.getCategory();
    }

    default String mapThirdCategory(FileObject fileObject) {
        String thirdLevelCategory = fileObject.getThirdLevelCategory();
        if (StrUtil.isBlank(thirdLevelCategory)) {
            return null;
        }
        return TagHistoryCategory.getCategoryByCode(convert(thirdLevelCategory));
    }

    default String mapSecondCategory(FileObject fileObject) {
        String subCategory = fileObject.getSubCategory();
        if (StrUtil.isBlank(subCategory)) {
            return null;
        }
        return SubCategoryEnum.getDescriptionByCodes(convert(subCategory));
    }

    default String mapCategoryIdentifier(FileObject fileObject) {
        String thirdLevelCategoryCode = fileObject.getThirdLevelCategory();
        if (StrUtil.isNotBlank(thirdLevelCategoryCode)) {
            String categoryIdentifier = FileCategoryAbilityAssociation.getCategoryIdentifierByTagHistoryCode(convert(thirdLevelCategoryCode));
            if (StrUtil.isNotBlank(categoryIdentifier)) {
                return categoryIdentifier;
            }
        }
        String subCategoryCode = fileObject.getSubCategory();
        if (StrUtil.isNotBlank(subCategoryCode)) {
            String categoryIdentifier = FileCategoryAbilityAssociation.getCategoryIdentifierBySubCategoryCode(convert(subCategoryCode));
            if (StrUtil.isNotBlank(categoryIdentifier)) {
                return categoryIdentifier;
            }
        }
        return null;
    }


    default List<Integer> convert(String convert) {
        return Arrays.stream(convert.split(","))
            .filter(NumberUtil::isNumber)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }

}
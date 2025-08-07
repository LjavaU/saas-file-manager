package com.supcon.tptrecommend.convert.fileobject;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.supcon.tptrecommend.common.enums.FileCategoryAbilityAssociation;
import com.supcon.tptrecommend.common.enums.TagHistoryCategory;
import com.supcon.tptrecommend.common.utils.FileUtils;
import com.supcon.tptrecommend.dto.fileobject.FileObjectCreateReq;
import com.supcon.tptrecommend.dto.fileobject.FileObjectResp;
import com.supcon.tptrecommend.entity.FileObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Mapping(target = "category", expression = "java(mapCategory(fileObject))")
    @Mapping(target = "categoryIdentifier", expression = "java(mapCategoryIdentifier(fileObject))")
    FileObjectResp convert(FileObject fileObject);

    default String mapFileSize(Long fileSize) {
        return FileUtils.formatFileSize(fileSize);

    }

    default String mapCategory(FileObject fileObject) {
        String thirdLevelCategoryCode = fileObject.getThirdLevelCategory();
        if (StrUtil.isNotBlank(thirdLevelCategoryCode)) {
            String categoryName = TagHistoryCategory.getCategoryByCode(convert(thirdLevelCategoryCode));
            if (categoryName != null) {
                return categoryName;
            }
        }
        String subCategoryCode = fileObject.getSubCategory();
        if (StrUtil.isNotBlank(subCategoryCode)) {
            String categoryName = FileCategoryAbilityAssociation.getCategoryNameBySubCategoryCode(convert(subCategoryCode));
            if (categoryName != null) {
                return categoryName;
            }
        }
        return fileObject.getCategory();
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
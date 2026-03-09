package com.example.saasfile.manager.strategy;


import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public interface BusinessDataHandler {

    
    Integer getBusinessKey();

    
    default Class<?> getEntityClass(){
        throw new UnsupportedOperationException();
    }



    
    default Map<String, String> getDbSchemaDescription() {
        Class<?> entityClass = getEntityClass();
        Map<String, String> schema = new LinkedHashMap<>();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ApiModelProperty.class)) {
                ApiModelProperty annotation = field.getAnnotation(ApiModelProperty.class);
                String fieldName = field.getName();
                String description = annotation.value();
                schema.put(description, fieldName);
            } else if (field.isAnnotationPresent(ExcelProperty.class)) {
                ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                String fieldName = field.getName();
                String description = annotation.value()[0];
                schema.put(description, fieldName);
            }
        }
        return schema;
    }

    
    default <T, R> List<R> castTargetObject(List<T> dataList, Class<R> targetType) {
       return dataList.stream()
            .filter(targetType::isInstance)
            .map(targetType::cast)
            .collect(Collectors.toList());
    }

    
    void batchSave(List<Object> dataList);


    
    default boolean isDirectHandler() {
        return false;
    }

    
    default void processDirectly(File file, Long fileId, int rowCount) {
        throw new UnsupportedOperationException();
    }

}
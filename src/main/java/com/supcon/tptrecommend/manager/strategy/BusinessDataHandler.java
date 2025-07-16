package com.supcon.tptrecommend.manager.strategy;


import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 业务数据处理器接口
 * 每个实现类负责处理一个特定的业务导入场景
 *
 * @author luhao
 * @since 2025/06/24 16:42:57
 */
public interface BusinessDataHandler {

    /**
     * 获取业务标识符
     *
     * @return 返回该处理器能处理的业务唯一标识符 (与LLM的返回一致)
     * @author luhao
     * @since 2025/06/24 16:44:31
     */
    Integer getBusinessKey();

    /**
     * 获取实体类
     *
     * @return 返回该业务对应的实体类
     * @author luhao
     * @since 2025/06/24 16:44:51
     */
    default Class<?> getEntityClass(){
        throw new UnsupportedOperationException();
    }



    /**
     * 获取数据库表结构描约束
     *
     * @return 返回该业务对应的数据库表结构描述 (用于发给LLM进行字段映射)
     * Key: 字段的中文描述/注释, Value: 实体属性名
     * @author luhao
     * @since 2025/06/24 16:45:00
     */
    default Map<String, String> getDbSchemaDescription() {
        Class<?> entityClass = getEntityClass();
        Map<String, String> schema = new LinkedHashMap<>();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            // 从ApiModelProperty注解中获取字段描述
            if (field.isAnnotationPresent(ApiModelProperty.class)) {
                ApiModelProperty annotation = field.getAnnotation(ApiModelProperty.class);
                String fieldName = field.getName();
                String description = annotation.value();
                schema.put(description, fieldName);
            } else if (field.isAnnotationPresent(ExcelProperty.class)) { // 从ExcelProperty注解中获取字段描述
                ExcelProperty annotation = field.getAnnotation(ExcelProperty.class);
                String fieldName = field.getName();
                String description = annotation.value()[0];
                schema.put(description, fieldName);
            }
        }
        return schema;
    }

    /**
     * 转换目标对象
     *
     * @param dataList   数据列表
     * @param targetType 目标类型
     * @return {@link List }<{@link R }>
     * @author luhao
     * @since 2025/06/27 15:24:00
     */
    default <T, R> List<R> castTargetObject(List<T> dataList, Class<R> targetType) {
       return dataList.stream()
            .filter(targetType::isInstance)
            .map(targetType::cast)
            .collect(Collectors.toList());
    }

    /**
     * 批量保存
     *
     * @param dataList 经过映射和转换的数据列表
     * @author luhao
     * @since 2025/06/24 16:45:35
     */
    void batchSave(List<Object> dataList);


    /**
     * 是否油该handle直接处理
     * 默认为false
     * 默认情况下，会先处理Excel数据，然后调用LLM进行数据和实体映射，最后再调用该方法进行数据保存
     * 如果该方法返回true，则直接调用该handle进行处理数据
     *
     * @return boolean
     * @author luhao
     * @since 2025/06/25 14:43:03
     */
    default boolean isDirectHandler() {
        return false;
    }

    /**
     * 它包含对数据特定处理逻辑。
     * 此方法将负责读取文件、转换数据并保存数据。
     * 默认触发 UnsupportedOperationException，因为只有直接处理程序才能实现它。
     *
     * @param file     文件
     * @param rowCount 文件总行数
     * @author luhao
     * @since 2025/06/25 15:04:30
     */
    default void processDirectly(File file, Long fileId, int rowCount) {
        throw new UnsupportedOperationException();
    }

}
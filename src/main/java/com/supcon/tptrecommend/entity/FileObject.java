package com.supcon.tptrecommend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.supcon.system.base.entity.BasicEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
@TableName("file_object")
@ApiModel(value = "MinIO 文件元数据表-数据库基础实体", description = "MinIO 文件元数据表")
public class FileObject extends BasicEntity<Long> {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户 ID", example = "用户 ID")
    private Long userId;

    @ApiModelProperty(value = "租户 ID", example = "租户 ID")
    private String tenantId;

    @ApiModelProperty(value = "用户 名称", example = "用户 名称")
    private String userName;

    @ApiModelProperty(value = "MinIO 中对象的 key（路径）", example = "MinIO 中对象的 key（路径）")
    private String objectName;

    @ApiModelProperty(value = "原始文件名", example = "原始文件名")
    private String originalName;

    @ApiModelProperty(value = "桶名称", example = "桶名称")
    private String bucketName;

    @ApiModelProperty(value = "MIME 文件类型", example = "MIME 文件类型")
    private String contentType;

    @ApiModelProperty(value = "文件大小（字节）")
    private Long fileSize;

    @ApiModelProperty(value = "标签", example = "标签")
    private String tags;

    @ApiModelProperty(value = "所属分类")
    private String category;


    @ApiModelProperty(value = "对应能力&应用")
    private String ability;


    @ApiModelProperty(value = "内容概述")
    private String contentOverview;

    @ApiModelProperty(value = "文件解析状态，【0-未解析，1-解析完成，2-解释失败】")
    private Integer fileStatus;

    @ApiModelProperty(value = "所属子类")
    private Integer subCategory;

    @AllArgsConstructor
    @Getter
    public enum FileStatus {
        UNPARSED(0, "未解析"),
        PARSED(1, "解析完成"),
        PARSE_FAILED(2, "解析失败");

        private final Integer value;
        private final String desc;
    }


    @AllArgsConstructor
    @Getter
    public enum Category {
        SYSTEM("0","系统配置"),
        BASIC_DATA("1","基础数据"),
        DYNAMIC_DATA("2","动态数据"),
        BUSINESS_DATA("3","业务数据");

        private final String code;
        private final String value;

        // 根据code获取value
        public static String getValueByCode(String code) {
            for (Category category : Category.values()) {
                if (category.code.equals(code)) {
                    return category.value;
                }
            }
            return null;
        }
    }

}
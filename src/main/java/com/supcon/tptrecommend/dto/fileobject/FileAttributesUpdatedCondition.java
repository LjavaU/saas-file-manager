package com.supcon.tptrecommend.dto.fileobject;

import lombok.Data;

@Data
public class FileAttributesUpdatedCondition {

    /**
     * 二级分类
     */
    private String subCategory;

    /**
     * 三级分类
     */
    private String thirdLevelCategory;

    /**
     * 能力
     */
    private String ability;

    /**
     * 对象名称
     */
    private String objectName;
}

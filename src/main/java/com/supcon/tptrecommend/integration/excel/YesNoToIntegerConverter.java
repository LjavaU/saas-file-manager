package com.supcon.tptrecommend.integration.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class YesNoToIntegerConverter implements Converter<Integer> {

    private static final String YES = "是";
    private static final String NO = "否";

    @Override
    public Class<?> supportJavaTypeKey() {
        // 此转换器支持的Java字段类型
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        // 此转换器支持的Excel单元格数据类型
        // 下拉框通常是字符串
        return CellDataTypeEnum.STRING;
    }

    /**
     * 从Excel单元格数据转换为Java对象 (读取时调用)
     * @param cellData Excel单元格数据，注意这里是 ReadCellData 不是 CellData
     * @param contentProperty 当前操作的属性（字段）元数据
     * @param globalConfiguration 全局配置
     * @return 转换后的Java对象 (Integer)
     */
    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) throws Exception {
        String stringValue = cellData.getStringValue();
        if (stringValue == null) {
            return null;
        }
        switch (stringValue.trim()) {
            case YES:
                return 1;
            case NO:
                return 0;
            default:
                return null;
        }
    }

    /**
     * 从Java对象转换为Excel单元格数据 (写入时调用)
     * @param value Java字段的值 (Integer)
     * @param contentProperty 当前操作的属性（字段）元数据
     * @param globalConfiguration 全局配置
     * @return 转换后的Excel单元格数据
     */
    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) throws Exception {
        if (value == null) {
            return new WriteCellData<>("");
        }
        switch (value) {
            case 1:
                return new WriteCellData<>(YES);
            case 0:
                return new WriteCellData<>(NO);
            default:
                return new WriteCellData<>("" );
        }
    }


}
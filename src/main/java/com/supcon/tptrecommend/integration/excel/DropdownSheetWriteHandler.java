package com.supcon.tptrecommend.integration.excel;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import java.util.Map;

@Slf4j
public class DropdownSheetWriteHandler implements SheetWriteHandler {

    // K:列的索引(0-based), V:下拉选项数组
    private final Map<Integer, String[]> dropdownMap;
    // 数据验证要应用的起始行（通常是表头下一行，即索引为1）
    private final int firstRow;
    // 数据验证要应用的结束行（可以设置一个较大的值，比如Excel最大行数附近，或者一个合理的业务上限）
    private final int lastRow;


    /**
     * 构造函数
     * @param dropdownMap key: 列的索引 (0-based), value: 下拉选项数组
     * @param firstDataRowIndex 数据验证应用的起始行索引 (0-based, 通常是表头行+1)
     * @param lastDataRowIndex  数据验证应用的最大行索引 (0-based)
     */
    public DropdownSheetWriteHandler(Map<Integer, String[]> dropdownMap, int firstDataRowIndex, int lastDataRowIndex) {
        this.dropdownMap = dropdownMap;
        this.firstRow = firstDataRowIndex; // 比如数据从第二行开始，索引就是1
        this.lastRow = lastDataRowIndex;   // 比如应用到第1001行，索引就是1000
    }

    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        // 可选: 在创建Sheet前做一些事情
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        log.info("Sheet创建完成，开始设置下拉列表约束。");
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();

        dropdownMap.forEach((columnIndex, dropdownValues) -> {
            if (dropdownValues == null || dropdownValues.length == 0) {
                log.warn("列 {} 的下拉选项为空，跳过。", columnIndex);
                return;
            }

            // 设置数据有效性加载在哪个单元格上，四个参数分别是：起始行、终止行、起始列、终止列
            CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);

            // 设置下拉列表的内容
            DataValidationConstraint constraint = helper.createExplicitListConstraint(dropdownValues);
            DataValidation dataValidation = helper.createValidation(constraint, addressList);

            // 处理Excel兼容性问题（如果下拉框数据很多，需要设置）
            if (dataValidation instanceof XSSFDataValidation) {
                dataValidation.setSuppressDropDownArrow(true); // 是否显示下拉箭头
                dataValidation.setShowErrorBox(true);         // 是否显示错误提示框
            } else {
                dataValidation.setSuppressDropDownArrow(false);
            }
            // 设置错误提示
            dataValidation.createErrorBox("输入错误", "请从下拉列表中选择一个有效值！");
            // 可设置输入提示
            // dataValidation.createPromptBox("输入提示", "请选择或输入一个值");
            // dataValidation.setShowPromptBox(true);

            sheet.addValidationData(dataValidation);
            log.info("为列 {} 设置了下拉选项，共 {} 个，应用于行 {} 到 {}", columnIndex, dropdownValues.length, firstRow, lastRow);
        });
    }
}
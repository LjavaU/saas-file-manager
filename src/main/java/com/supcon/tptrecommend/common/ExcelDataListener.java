package com.supcon.tptrecommend.common;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel读取数据行的监听器
 *
 * @author luhao
 * @date 2025/06/09 16:04:32
 */
@Slf4j
@Getter
public class ExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    // 存储所有行数据，包括表头
    private final List<List<String>> data = new ArrayList<>();
    // 存储表头，用于确定列数
    private List<String> header = new ArrayList<>();

    /**
     * 从 Excel文件中读取的每一行数据都会调用此方法。
     *
     * @param rowData 当前行的数据，作为列索引到单元格值的映射。
     * @param context 分析上下文.
     */
    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        // 将 Map 的 values 转换为 List
        List<String> rowList = new ArrayList<>(rowData.values());
        // TODO: 限制记录是为10条
        if (rowList.size() == 11) {
            return;
        }
        data.add(rowList);
    }

    /**
     * 当分析完所有数据后，将调用该方法。
     *
     * @param context 分析上下文.
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel 解析已完成。总行数： {}", data.size());
    }

    /**
     * 读取 header 时调用该方法。
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.header = new ArrayList<>(headMap.values());
        //EasyExcel 的默认行为是读取 Header，然后从下一行开始 'invoke'，手动将标头添加到数据列表中，以确保它是第一个元素。
        data.add(this.header);
    }
}
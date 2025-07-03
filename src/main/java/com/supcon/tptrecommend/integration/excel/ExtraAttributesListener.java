package com.supcon.tptrecommend.integration.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Excel(CSV)侦听器
 * 获取文件的表头或者数据行数
 *
 * @author luhao
 * @since 2025/06/26 16:19:10
 */
@Getter
public class ExtraAttributesListener  extends AnalysisEventListener<Map<Integer, String>> {

    private int rowCount = 0;

    private final List<List<String>> originalHeaders = new ArrayList<>();


    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        // 每读取一行数据（不含表头），计数器加一
        rowCount++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }


    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        originalHeaders.add(new ArrayList<>(headMap.values()));
    }

}
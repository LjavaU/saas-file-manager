package com.example.saasfile.integration.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@Getter
public class ExtraAttributesListener  extends AnalysisEventListener<Map<Integer, String>> {

    private int rowCount = 0;

    private final List<List<String>> originalHeaders = new ArrayList<>();


    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
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
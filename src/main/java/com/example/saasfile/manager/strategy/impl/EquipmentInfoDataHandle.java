package com.example.saasfile.manager.strategy.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.utils.MarkdownConverter;
import com.example.saasfile.feign.LlmFeign;
import com.example.saasfile.feign.entity.llm.FileEquipmentExtractReq;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class EquipmentInfoDataHandle implements BusinessDataHandler {

    private final LlmFeign llmFeign;

    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.EQUIPMENT_INFO.getCode();
    }

    @Override
    public void batchSave(List<Object> dataList) {

    }

    @Override
    public boolean isDirectHandler() {
        return true;
    }

    @Override
    public void processDirectly(File file, Long fileId, int rowCount) {
       /* EasyExcel.read(file, new EquipmentInfoDataHandle.DataListener())
            .sheet()
            .doRead();*/
    }

    class DataListener extends AnalysisEventListener<Map<Integer, String>> {

        int rowNumberThreshold = 20;
        List<List<String>> results = new ArrayList<>();

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            List<String> rowData = new ArrayList<>(data.values());
            results.add(rowData);
            if (results.size() == rowNumberThreshold) {
                String markdownContent = MarkdownConverter.generateMarkdownTable(results);
                log.info("Converted markdown: {}", markdownContent);
                Object result = llmFeign.ner(FileEquipmentExtractReq.builder()
                    .documentType("excel")
                    .markdownContent(markdownContent)
                    .subcategory(SubCategoryEnum.EQUIPMENT_INFO.getCode())
                    .build());
                log.info("LLM extraction result: {}", result);
                results.clear();
            }
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            if (!results.isEmpty()) {
                String markdownContent = MarkdownConverter.generateMarkdownTable(results);
                log.info("Converted markdown: {}", markdownContent);
            }
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {

        }
    }
}
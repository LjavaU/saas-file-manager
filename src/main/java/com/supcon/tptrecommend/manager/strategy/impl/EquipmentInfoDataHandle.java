package com.supcon.tptrecommend.manager.strategy.impl;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.utils.MarkdownConverter;
import com.supcon.tptrecommend.feign.LlmFeign;
import com.supcon.tptrecommend.feign.entity.llm.FileEquipmentExtractReq;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
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
            // 处理每一行数据
            List<String> rowData = new ArrayList<>(data.values());
            results.add(rowData);
            if (results.size() == rowNumberThreshold) {
                String markdownContent = MarkdownConverter.generateMarkdownTable(results);
                log.info("转换后的markdown: {}", markdownContent);
                Object result = llmFeign.ner(FileEquipmentExtractReq.builder()
                    .documentType("excel")
                    .markdownContent(markdownContent)
                    .subcategory(SubCategoryEnum.EQUIPMENT_INFO.getCode())
                    .build());
                log.info("调用大模型返回结果: {}", result);
                results.clear();
            }

        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            if (!results.isEmpty()) {
                String markdownContent = MarkdownConverter.generateMarkdownTable(results);
                log.info("转换后的markdown: {}", markdownContent);
            }

        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {

        }
    }
}

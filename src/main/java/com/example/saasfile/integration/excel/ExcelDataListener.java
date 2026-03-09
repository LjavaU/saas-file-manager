package com.example.saasfile.integration.excel;


import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.saasfile.common.Constants;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.convert.filedata.DynamicMapper;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import com.example.saasfile.service.IFileObjectService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Getter
public class ExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {

    
    private final Map<String, String> excelHeaderToEntityFieldMap;
    
    private final BusinessDataHandler handler;
    
    private final ObjectMapper objectMapper;
    
    private List<String> originalHeaders;
    
    private final List<Object> entityList = new ArrayList<>();
    
    private final int totalCount;
    
    private int lastReportedProgress = -1;
    
    private final int startProgress = 40;
    
    private final Long fileId;
    
    private final DynamicMapper<Object, Object> dynamicMapper;

    
    private final Long userId;

    public ExcelDataListener(Map<String, String> mapping, BusinessDataHandler handler, Long fileId, int totalCount, DynamicMapper<Object, Object> mapper, Long userId) {
        this.excelHeaderToEntityFieldMap = mapping;
        this.handler = handler;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.fileId = fileId;
        this.totalCount = totalCount;
        this.dynamicMapper = mapper;
        this.userId = userId;
    }

    
    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        Map<String, Object> entityPropertyMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
            String excelHeader = originalHeaders.get(entry.getKey());
            String entityField = excelHeaderToEntityFieldMap.get(excelHeader);

            if (entityField != null) {
                entityPropertyMap.put(entityField, entry.getValue());
            }
        }
        if (entityPropertyMap.isEmpty()) {
            return;
        }
        try {
            if (dynamicMapper != null) {
                entityList.add(dynamicMapper.map(entityPropertyMap));
            } else {
                Object entity = objectMapper.convertValue(entityPropertyMap, handler.getEntityClass());
                entityList.add(entity);
            }

        } catch (Exception e) {
            log.error("鏁版嵁杞崲澶辫触:{}", e.getMessage());
        }
        if (entityList.size() >= Constants.INSERT_SIZE) {
            saveData();
            entityList.clear();
            calculateProgress(context);
        }


    }

    private void calculateProgress(AnalysisContext context) {
        ReadRowHolder rowHolder = context.readRowHolder();
        int currentRowNum = rowHolder.getRowIndex();
        if (totalCount > 0) {
            int progress = ProcessProgressSupport.calculateFromStartProgress(currentRowNum, totalCount, startProgress);
            if (progress > lastReportedProgress) {
                ProcessProgressSupport.notifyParseProcessing(fileId,userId, progress);
                lastReportedProgress = progress;
            }
        }
    }


    
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!entityList.isEmpty()) {
            saveData();
        }
        entityList.clear();
        IFileObjectService fileObjectService = SpringUtil.getBean(IFileObjectService.class);
        fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
        ProcessProgressSupport.notifyParseComplete(fileId,userId);
    }

    private void saveData() {
        handler.batchSave(entityList);
    }


    
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.originalHeaders = new ArrayList<>(headMap.values());
    }
}
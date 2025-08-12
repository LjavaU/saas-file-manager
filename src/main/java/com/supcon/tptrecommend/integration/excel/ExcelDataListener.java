package com.supcon.tptrecommend.integration.excel;


import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supcon.tptrecommend.common.Constants;
import com.supcon.tptrecommend.common.enums.FileStatus;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.convert.filedata.DynamicMapper;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * LLM返回的文件表头映射关系
     * key：Excel表头 value：实体属性名
     */
    private final Map<String, String> excelHeaderToEntityFieldMap;
    /**
     * 业务数据处理器
     */
    private final BusinessDataHandler handler;
    /**
     * Jackson的核心转换器
     */
    private final ObjectMapper objectMapper;
    /**
     * 文件表头
     */
    private List<String> originalHeaders;
    /**
     * 存放转换好的实体对象
     */
    private final List<Object> entityList = new ArrayList<>();
    /**
     * 文件总行数
     */
    private final int totalCount;
    /**
     * 上次处理进度
     */
    private int lastReportedProgress = -1;
    /**
     * 开始进度
     */
    private final int startProgress = 40;
    /**
     * 文件 ID
     */
    private final Long fileId;
    /**
     * 自定义数据动态映射器
     */
    private final DynamicMapper<Object, Object> dynamicMapper;

    /**
     * 用户 ID
     */
    private final Long userId;

    public ExcelDataListener(Map<String, String> mapping, BusinessDataHandler handler, Long fileId, int totalCount, DynamicMapper<Object, Object> mapper, Long userId) {
        this.excelHeaderToEntityFieldMap = mapping;
        this.handler = handler;
        // 初始化ObjectMapper，并注册Java 8时间模块以正确处理日期
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.fileId = fileId;
        this.totalCount = totalCount;
        this.dynamicMapper = mapper;
        this.userId = userId;
        // 如果实体属性是驼峰命名(camelCase)，而数据库是下划线(snake_case)，可以在此配置转换策略
        // this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    /**
     * 从 Excel文件中读取的每一行数据都会调用此方法。
     *
     * @param rowData 当前行的数据，作为列索引到单元格值的映射。
     * @param context 分析上下文.
     */
    @Override
    public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        // 1. 将Excel行数据(Key是列索引)转换为Key是实体属性名的Map
        Map<String, Object> entityPropertyMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
            String excelHeader = originalHeaders.get(entry.getKey());
            // 根据Excel表头，从映射关系中找到对应的实体属性名
            String entityField = excelHeaderToEntityFieldMap.get(excelHeader);

            if (entityField != null) {
                // Key是实体属性名(e.g., "customerName"), Value是Excel单元格的值
                entityPropertyMap.put(entityField, entry.getValue());
            }
        }
        // 如果这一行是空的或无法映射，则跳过
        if (entityPropertyMap.isEmpty()) {
            return;
        }
        try {
            // 使用自定义映射器进行数据转换
            if (dynamicMapper != null) {
                entityList.add(dynamicMapper.map(entityPropertyMap));
            } else {
                //使用ObjectMapper将Map转换为对应的实体对象！handler.getEntityClass() 动态地告诉ObjectMapper要转换成哪个类的实例
                Object entity = objectMapper.convertValue(entityPropertyMap, handler.getEntityClass());
                entityList.add(entity);
            }

        } catch (Exception e) {
            log.error("数据转换失败:{}", e.getMessage());
        }
        // 3. 达到批处理阈值，执行插入
        if (entityList.size() >= Constants.INSERT_SIZE) {
            saveData();
            entityList.clear();
            calculateProgress(context);
        }


    }

    private void calculateProgress(AnalysisContext context) {
        // 从 context 获取当前行号和总行数
        ReadRowHolder rowHolder = context.readRowHolder();
        int currentRowNum = rowHolder.getRowIndex();// 行号从0开始
        // 计算并报告进度
        if (totalCount > 0) {
            int progress = ProcessProgressSupport.calculateFromStartProgress(currentRowNum, totalCount, startProgress);
            if (progress > lastReportedProgress) {
                ProcessProgressSupport.notifyParseProcessing(fileId,userId, progress);
                lastReportedProgress = progress;
            }
        }
    }


    /**
     * 当分析完所有数据后，将调用该方法。
     *
     * @param context 分析上下文.
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!entityList.isEmpty()) {
            saveData();
        }
        entityList.clear();
        // 更新文件解析状态为成功
        IFileObjectService fileObjectService = SpringUtil.getBean(IFileObjectService.class);
        fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
        ProcessProgressSupport.notifyParseComplete(fileId,userId);
    }

    private void saveData() {
        handler.batchSave(entityList);
    }


    /**
     * 读取 header 时调用该方法。
     * EasyExcel 的默认行为是读取 Header，然后从下一行开始 'invoke'，
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.originalHeaders = new ArrayList<>(headMap.values());
    }
}
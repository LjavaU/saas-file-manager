package com.supcon.tptrecommend.manager.strategy.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.supcon.systemcommon.entity.SupRequestBody;
import com.supcon.systemcommon.entity.SupResult;
import com.supcon.tptrecommend.common.Constants;
import com.supcon.tptrecommend.common.enums.SubCategoryEnum;
import com.supcon.tptrecommend.common.utils.DateParserUtil;
import com.supcon.tptrecommend.common.utils.ProcessProgressSupport;
import com.supcon.tptrecommend.entity.FileObject;
import com.supcon.tptrecommend.feign.DataHubFeign;
import com.supcon.tptrecommend.feign.entity.datahub.TagValueDTO;
import com.supcon.tptrecommend.manager.strategy.BusinessDataHandler;
import com.supcon.tptrecommend.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 位号历史数据处理器
 *
 * @author luhao
 * @since 2025/06/25 18:30:32
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TagHistoryDataHandler implements BusinessDataHandler {

    private final DataHubFeign dataHubFeign;

    private final IFileObjectService fileObjectService;


    @Override
    public Integer getBusinessKey() {
        return SubCategoryEnum.TAG_HISTORICAL_DATA.getCode();
    }

    @Override
    public Class<?> getEntityClass() {
        return TagValueDTO.class;
    }

    @Override
    public void batchSave(List<Object> dataList) {
        List<TagValueDTO> tagValueDTOS = castTargetObject(dataList, TagValueDTO.class);
        SupResult<Boolean> supResult = dataHubFeign.importTagValue(SupRequestBody.data(tagValueDTOS));
        if (!supResult.getSuccess()) {
            log.error("位号历史数据保存失败：{}", supResult.getMsg());
        }
    }

    @Override
    public boolean isDirectHandler() {
        return true;
    }

    @Override
    public void processDirectly(File file, Long fileId, int rowCount) {
        EasyExcel.read(file, new DataListener(fileId, rowCount))
            .sheet()
            .doRead();
    }

    class DataListener extends AnalysisEventListener<Map<Integer, String>> {
        private List<String> headers;
        private final int totalCount;
        List<Object> entities = new ArrayList<>();
        private int Thresholds;
        private final Long fileId;
        private int lastReportedProgress = -1;

        public DataListener(Long fileId, int totalCount) {
            this.fileId = fileId;
            this.totalCount = totalCount;
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            String time = data.get(0);
            if(NumberUtil.isNumber( time)){
                return;
            }
            data.remove(0);
            LocalDateTime dateTime = DateParserUtil.parse(time);
            for (Map.Entry<Integer, String> entry : data.entrySet()) {
                if (!NumberUtil.isNumber(entry.getValue())) {
                    continue;
                }
                TagValueDTO tagValueDTO = new TagValueDTO();
                tagValueDTO.setQuality(192L);
                tagValueDTO.setTagName(headers.get(entry.getKey()));
                tagValueDTO.setTagValue(entry.getValue());
                if (StrUtil.isNotBlank(time)) {
                    tagValueDTO.setTagTime(dateTime);
                    tagValueDTO.setAppTime(dateTime);
                }
                entities.add(tagValueDTO);
            }
            if (entities.size() >= Constants.TAG_HISTORY_VALUE_INSERT_SIZE) {
                batchSave(entities);
                entities.clear();
            }
            calculateProgress(context);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            if (!entities.isEmpty()) {
                batchSave(entities);
            }
            entities.clear();
            // 更新文件解析状态为成功
            fileObjectService.updateFileParseStatus(fileId, FileObject.FileStatus.PARSED);
            ProcessProgressSupport.notifyParseComplete(fileId);
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            this.headers = new ArrayList<>(headMap.values());
            Thresholds = totalCount / 10;
        }

        private void calculateProgress(AnalysisContext context) {
            // 从 context 获取当前行号和总行数
            ReadRowHolder rowHolder = context.readRowHolder();
            int currentRowNum = rowHolder.getRowIndex();// 行号从0开始
            // 计算并报告进度
            if (Thresholds != 0 && currentRowNum % Thresholds == 0 && totalCount > 0) {
                int startProgress = 20;
                int progress = ProcessProgressSupport.calculateFromStartProgress(currentRowNum, totalCount, startProgress);
                if (progress > lastReportedProgress) {
                    ProcessProgressSupport.notifyParseProcessing(fileId, progress);
                    lastReportedProgress = progress;
                }
            }
        }
    }
}

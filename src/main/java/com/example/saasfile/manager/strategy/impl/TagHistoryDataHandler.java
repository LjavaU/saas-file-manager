package com.example.saasfile.manager.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.example.saasfile.support.web.SupRequestBody;
import com.example.saasfile.support.web.SupResult;
import com.example.saasfile.common.Constants;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.utils.DateParserUtil;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.feign.DataHubFeign;
import com.example.saasfile.feign.entity.datahub.TagValueDTO;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import com.example.saasfile.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;


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
        SupResult<Map<String, Collection<String>>> supResult = dataHubFeign.importTagValue(SupRequestBody.data(tagValueDTOS));
        if (!supResult.isSuccess()) {
            log.error("Import tag history data failed: {}", supResult.getMsg());
        }
    }

    @Override
    public boolean isDirectHandler() {
        return true;
    }

    @Override
    public void processDirectly(File file, Long fileId, int rowCount) {
        Long userId = fileObjectService.getUserIdByFileId(fileId);
        if (Objects.isNull(userId)) {
            return;
        }
        EasyExcel.read(file, new DataListener(fileId, rowCount, userId))
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
        private final Long userId;

        public DataListener(Long fileId, int totalCount, Long userId) {
            this.fileId = fileId;
            this.totalCount = totalCount;
            this.userId = userId;
        }

        @Override
        public void invoke(Map<Integer, String> data, AnalysisContext context) {
            String time = data.get(0);
            LocalDateTime dateTime = DateParserUtil.parse(time);
            if (Objects.isNull(dateTime)) {
                return;
            }
            data.remove(0);
            for (Map.Entry<Integer, String> entry : data.entrySet()) {
                if (entry.getKey() >= headers.size()) {
                    continue;
                }
                String tagName = headers.get(entry.getKey());
                if (StrUtil.isBlank(tagName)) {
                    continue;
                }
                TagValueDTO tagValueDTO = new TagValueDTO();
                tagValueDTO.setQuality(192L);
                tagValueDTO.setTagName(tagName);
                tagValueDTO.setTagValue(entry.getValue());
                tagValueDTO.setTagTime(dateTime);
                tagValueDTO.setAppTime(dateTime);
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
                entities.clear();
            }
            fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
            ProcessProgressSupport.notifyParseComplete(fileId, userId);
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            this.headers = new ArrayList<>(headMap.values());
            Thresholds = totalCount / 10;
        }

        private void calculateProgress(AnalysisContext context) {
            ReadRowHolder rowHolder = context.readRowHolder();
            int currentRowNum = rowHolder.getRowIndex();
            if (Thresholds != 0 && currentRowNum % Thresholds == 0 && totalCount > 0) {
                int startProgress = 20;
                int progress = ProcessProgressSupport.calculateFromStartProgress(currentRowNum, totalCount, startProgress);
                if (progress > lastReportedProgress) {
                    ProcessProgressSupport.notifyParseProcessing(fileId, userId, progress);
                    lastReportedProgress = progress;
                }
            }
        }
    }
}

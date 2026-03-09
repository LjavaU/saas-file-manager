package com.example.saasfile.manager.strategy.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.google.common.collect.Sets;
import com.example.saasfile.common.enums.FileCategory;
import com.example.saasfile.common.enums.FileCategoryAbilityAssociation;
import com.example.saasfile.common.enums.FileStatus;
import com.example.saasfile.common.enums.SubCategoryEnum;
import com.example.saasfile.common.enums.TagHistoryCategory;
import com.example.saasfile.common.utils.FileUtils;
import com.example.saasfile.common.utils.MarkdownConverter;
import com.example.saasfile.common.utils.MinioUtils;
import com.example.saasfile.common.utils.ProcessProgressSupport;
import com.example.saasfile.common.utils.RandomUtil;
import com.example.saasfile.support.exception.ServerException;
import com.example.saasfile.convert.filedata.DynamicMapper;
import com.example.saasfile.entity.FileObject;
import com.example.saasfile.feign.LlmFeign;
import com.example.saasfile.feign.entity.llm.FileAlignmentReq;
import com.example.saasfile.feign.entity.llm.FileAlignmentResp;
import com.example.saasfile.feign.entity.llm.FileClassifyReq;
import com.example.saasfile.feign.entity.llm.FileClassifyResp;
import com.example.saasfile.integration.excel.ExcelDataListener;
import com.example.saasfile.integration.excel.ExtraAttributesListener;
import com.example.saasfile.manager.strategy.BusinessDataHandler;
import com.example.saasfile.manager.strategy.BusinessDataHandlerFactory;
import com.example.saasfile.manager.strategy.FileAnalysisHandle;
import com.example.saasfile.manager.strategy.MapperFactory;
import com.example.saasfile.service.IFileObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelFileAnalysisHandle implements FileAnalysisHandle {

    private final IFileObjectService fileObjectService;
    private final LlmFeign llmFeign;
    private final BusinessDataHandlerFactory businessDataHandlerFactory;
    private final MapperFactory mapperFactory;
    private final MinioUtils minioUtils;

    @Override
    public void handleFileAnalysis(Long fileId, Integer category) {
        FileObject fileObject = fileObjectService.getById(fileId);
        if (fileObject == null) {
            log.error("File does not exist. fileId={}", fileId);
            return;
        }
        ProcessProgressSupport.notifyParseProcessing(fileId, fileObject.getUserId(), RandomUtil.getRandomPercentage(5, 10));
        if (!FileStatus.UNPARSED.getValue().equals(fileObject.getFileStatus())) {
            return;
        }

        String originalFilename = fileObject.getOriginalName();
        String objectName = fileObject.getObjectName();
        String uniqueFilename = FileUtils.getFileNameFromObjectKey(objectName);
        File file = null;
        try {
            file = minioUtils.saveStreamToTempFile(fileObject.getBucketName(), objectName, uniqueFilename);
            if (file == null) {
                throw new ServerException("Failed to prepare local file for analysis");
            }
            doHandle(file, fileId, originalFilename, fileObject.getUserId(), category);
        } finally {
            FileUtils.deleteTemporaryFile(file, originalFilename);
        }
    }

    @Override
    public Set<String> getSupportedTypes() {
        return Sets.newHashSet("xlsx", "xls", "csv");
    }

    private void doHandle(File file, Long fileId, String originalFilename, Long userId, Integer category) {
        if (category != null) {
            businessDataHandlerFactory.getHandler(category).ifPresent(handler -> handler.processDirectly(file, fileId, 0));
            return;
        }

        ExtraAttributesListener extraAttributesListener = new ExtraAttributesListener();
        ExcelReaderBuilder readerBuilder = EasyExcel.read(file, extraAttributesListener);
        setCsvFileEncoding(file, FilenameUtils.getExtension(originalFilename), readerBuilder);
        readerBuilder.sheet().headRowNumber(5).doRead();

        List<List<String>> excelHeaders = extraAttributesListener.getOriginalHeaders();
        int rowCount = extraAttributesListener.getRowCount();
        String headerMarkdown = MarkdownConverter.generateMarkdownTable(excelHeaders);
        if (headerMarkdown == null) {
            throw new ServerException("Failed to build header markdown");
        }

        FileClassifyResp classifyResp = classifyFile(headerMarkdown, originalFilename);
        updateFileParseMetadata(fileId, classifyResp);
        ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(15, 20));

        Optional<BusinessDataHandler> handlerOptional = businessDataHandlerFactory.getHandler(classifyResp.getSubcategory());
        if (!handlerOptional.isPresent()) {
            updateFileParsed(fileId);
            ProcessProgressSupport.notifyParseComplete(fileId, userId);
            return;
        }

        BusinessDataHandler handler = handlerOptional.get();
        if (handler.isDirectHandler()) {
            handler.processDirectly(file, fileId, rowCount);
            return;
        }

        FileAlignmentResp alignmentResp = getFileHeaderMapping(headerMarkdown, JSONUtil.toJsonStr(handler.getDbSchemaDescription()), originalFilename);
        ProcessProgressSupport.notifyParseProcessing(fileId, userId, RandomUtil.getRandomPercentage(30, 40));
        Map<String, String> mapping = alignmentResp == null || alignmentResp.getData() == null
            ? Collections.emptyMap()
            : JSONUtil.toBean(alignmentResp.getData(), new TypeReference<Map<String, String>>() {}, true);
        DynamicMapper<Object, Object> dynamicMapper = mapperFactory.getMapper(classifyResp.getSubcategory());
        ExcelDataListener excelDataListener = new ExcelDataListener(mapping, handler, fileId, rowCount, dynamicMapper, userId);
        ExcelReaderBuilder read = EasyExcel.read(file, excelDataListener);
        setCsvFileEncoding(file, FilenameUtils.getExtension(originalFilename), read);
        read.sheet().doRead();
    }

    private void setCsvFileEncoding(File file, String fileSuffix, ExcelReaderBuilder readerBuilder) {
        if ("csv".equalsIgnoreCase(fileSuffix)) {
            readerBuilder.excelType(ExcelTypeEnum.CSV).charset(FileUtils.detectCharset(file));
        }
    }

    private FileAlignmentResp getFileHeaderMapping(String excelHeaderMarkdown, String dataBaseSchema, String originalFilename) {
        FileAlignmentResp alignmentResp = llmFeign.alignment(FileAlignmentReq.builder()
            .excelHeader(excelHeaderMarkdown)
            .databaseSchema(dataBaseSchema)
            .subcategory(0)
            .documentType("excel")
            .build());
        if (alignmentResp == null) {
            log.warn("LLM alignment returned null. file={}", originalFilename);
        }
        return alignmentResp;
    }

    private FileClassifyResp classifyFile(String headerMarkdown, String originalFilename) {
        FileClassifyResp classifyResp = llmFeign.classify(FileClassifyReq.builder()
            .headMarkdownContent(headerMarkdown)
            .documentType("excel")
            .build());
        if (classifyResp == null) {
            log.warn("LLM classification returned null. file={}", originalFilename);
            classifyResp = new FileClassifyResp();
            classifyResp.setCategory(FileCategory.BUSINESS_DATA.getCode());
            classifyResp.setSubcategory(SubCategoryEnum.METRICS_BUSINESS_REPORT_DATA.getCode());
            classifyResp.setSummary("");
        }
        return classifyResp;
    }

    private void updateFileParseMetadata(Long fileId, FileClassifyResp classifyResp) {
        FileObject fileObject = new FileObject();
        fileObject.setId(fileId);
        fileObject.setCategory(FileCategory.getValueByCode(classifyResp.getCategory()));
        fileObject.setContentOverview(classifyResp.getSummary());
        if (classifyResp.getSubcategory() != null) {
            fileObject.setSubCategory(String.valueOf(classifyResp.getSubcategory()));
        }
        Integer thirdLevelCategory = classifyResp.getThird_level_category();
        if (thirdLevelCategory != null && thirdLevelCategory != -1) {
            fileObject.setThirdLevelCategory(String.valueOf(thirdLevelCategory));
            fileObject.setAbility(FileCategoryAbilityAssociation.getAbilityByTagHistoryCategory(TagHistoryCategory.getByCode(thirdLevelCategory)));
        } else if (classifyResp.getSubcategory() != null) {
            fileObject.setAbility(FileCategoryAbilityAssociation.getAbilityBySubCategory(SubCategoryEnum.getByCode(classifyResp.getSubcategory())));
        }
        fileObjectService.updateById(fileObject);
    }

    private void updateFileParsed(Long fileId) {
        fileObjectService.updateFileParseStatus(fileId, FileStatus.PARSED);
    }
}
